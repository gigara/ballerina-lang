package org.ballerinalang.linter.Reference;

import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.tree.*;
import org.wso2.ballerinalang.compiler.tree.clauses.*;
import org.wso2.ballerinalang.compiler.tree.expressions.*;
import org.wso2.ballerinalang.compiler.tree.statements.*;
import org.wso2.ballerinalang.compiler.tree.types.*;

import java.util.*;

public class LinteringReferenceVisitor extends BLangNodeVisitor {
    private HashMap<Integer, Definition> definitions = new HashMap<>();

    public LinteringReferenceVisitor() {
    }

    public HashMap<Integer, Definition> getDefinitions() {
        return definitions;
    }

    private void acceptNode(BLangNode node) {
        node.accept(this);
    }

    // add definition to the arrayList
    private void addDefinition(BSymbol symbol, Diagnostic.DiagnosticPosition pos, Set<Whitespace> ws) {
        Symbol symbol1 = new Symbol(symbol.name.value, symbol.kind != null ? symbol.kind.name() : symbol.type.tsymbol.name.value,
                symbol.pkgID.name.value, symbol.pkgID.orgName.value);
        if (!availableInDefinitions(symbol1)) {
            Definition definition = new Definition(symbol1, false, true, getPosition(pos, ws, symbol.name.value));
            definitions.put(definition.hashCode(), definition);
        } else {
            definitions.get(symbol1.hashCode()).setHasDefinition(true);
        }
    }

    // add reference to the arrayList
    private void addReference(BSymbol symbol, Diagnostic.DiagnosticPosition pos, Set<Whitespace> ws) {
        Symbol symbol1 = new Symbol(symbol.name.value, symbol.kind != null ? symbol.kind.name() : symbol.type.tsymbol.name.value,
                symbol.pkgID.name.value, symbol.pkgID.orgName.value);
        if (availableInDefinitions(symbol1)) {
            definitions.get(symbol1.hashCode()).setHasReference(true);
        } else {
            Definition definition = new Definition(symbol1, true, false, getPosition(pos, ws, symbol.name.value));
            definitions.put(definition.hashCode(), definition);
        }
    }

    // search for the symbol in definitions list
    private boolean availableInDefinitions(Symbol symbol) {
        return definitions.containsKey(symbol.hashCode());
    }

    // diagnostic log end position
    private Diagnostic.DiagnosticPosition getPosition(Diagnostic.DiagnosticPosition position, Set<Whitespace> ws, String text) {
        if (ws == null) {
            return position;
        }
        position.setEndLine(position.getStartLine());
        position.setStartColumn(getStartPosition(ws, text));
        position.setEndColumn(getEndPosition(ws, text));
        return position;
    }

    private int getStartPosition(Set<Whitespace> ws, String text) {
        int count = 1;
        int to = 0;

        // find text
        Iterator<Whitespace> it = ws.iterator();
        while (it.hasNext()) {
            Whitespace current = it.next();
            if (current.getPrevious().equals(text))
                break;
            to++;
        }

        // count
        it = ws.iterator();
        int iteratePos = 0;
        while (it.hasNext()) {
            Whitespace current = it.next();
            String WStext = current.getPrevious();
            String WS = current.getWs().replace("\n", "");
            count += WStext.length();
            count += WS.length();

            if (++iteratePos == to)
                break;
        }

        return ++count;
    }

    private int getEndPosition(Set<Whitespace> ws, String text) {
        int count = 1;
        int to = 0;

        // find text
        Iterator<Whitespace> it = ws.iterator();
        while (it.hasNext()) {
            Whitespace current = it.next();
            if (current.getPrevious().equals(text))
                break;
            to++;
        }

        // count
        it = ws.iterator();
        int iteratePos = 0;
        while (it.hasNext()) {
            Whitespace current = it.next();
            String WStext = current.getPrevious();
            String WS = current.getWs().replace("\n", "");
            count += WStext.length();
            count += WS.length();

            if (++iteratePos > to)
                break;
        }

        return count;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        //
    }

    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
        //
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        List<TopLevelNode> topLevelNodes = compUnit.topLevelNodes;
        topLevelNodes.forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        //
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        addDefinition(xmlnsNode.symbol, xmlnsNode.pos, xmlnsNode.getWS());
    }

    @Override
    public void visit(BLangFunction funcNode) {
        addDefinition(funcNode.symbol, funcNode.pos, funcNode.getWS());
        funcNode.annAttachments.forEach(this::acceptNode);
        funcNode.requiredParams.forEach(this::acceptNode);
        funcNode.externalAnnAttachments.forEach(this::acceptNode);
        funcNode.returnTypeAnnAttachments.forEach(this::acceptNode);
        this.acceptNode(funcNode.returnTypeNode);
        this.acceptNode(funcNode.body);

    }

    @Override
    public void visit(BLangService serviceNode) {
        addDefinition(serviceNode.symbol, serviceNode.pos, serviceNode.getWS());
        serviceNode.annAttachments.forEach(this::acceptNode);
        if (serviceNode.attachedExprs != null) {
            serviceNode.attachedExprs.forEach(this::acceptNode);
        }
        serviceNode.resourceFunctions.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangResource resourceNode) {
        //
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        addDefinition(typeDefinition.symbol, typeDefinition.pos, typeDefinition.getWS());
        typeDefinition.annAttachments.forEach(this::acceptNode);
        // Visit the type node
        this.acceptNode(typeDefinition.typeNode);
    }

    @Override
    public void visit(BLangConstant constant) {
        addDefinition(constant.symbol, constant.pos, constant.getWS());
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        if (varNode.flagSet.contains(Flag.SERVICE)) {
            // Skip the anon service symbol generated for the BLangService,
            // which will be visited from BLangService visitor
            return;
        }
        addDefinition(varNode.symbol, varNode.pos, varNode.getWS());
        varNode.annAttachments.forEach(this::acceptNode);
        this.acceptNode(varNode.expr);
    }

    @Override
    public void visit(BLangWorker workerNode) {

    }

    @Override
    public void visit(BLangEndpoint endpointNode) {

    }

    @Override
    public void visit(BLangIdentifier identifierNode) {

    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        addDefinition(annotationNode.symbol, annotationNode.pos, annotationNode.getWS());
        annotationNode.annAttachments.forEach(this::acceptNode);
        this.acceptNode(annotationNode.typeNode);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        this.acceptNode(annAttachmentNode.expr);
        addReference(annAttachmentNode.annotationSymbol, annAttachmentNode.pos, annAttachmentNode.getWS());
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.getStatements().forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
        //
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
        //
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        BLangSimpleVariable variable = varDefNode.var;
        addDefinition(variable.symbol, varDefNode.pos, varDefNode.getWS());

        BLangType typeNode = variable.typeNode;
        if (varDefNode.getWS() == null) {
            if (varDefNode.var.expr instanceof BLangLambdaFunction
                    && ((BLangLambdaFunction) varDefNode.var.expr).function.flagSet.contains(Flag.WORKER)) {
                return;
            }
        } else {
            // In the foreach's variable definition node, type becomes null and will be handled by the acceptNode
            if (typeNode != null)
                this.acceptNode(typeNode);
        }

        // Visit the expression
        if (varDefNode.var.expr != null) {
            this.acceptNode(varDefNode.var.expr);
        }
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        this.acceptNode(assignNode.varRef);
        // Visit the expression
        this.acceptNode(assignNode.expr);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        this.acceptNode(compoundAssignNode.varRef);
        this.acceptNode(compoundAssignNode.expr);
    }

    @Override
    public void visit(BLangAbort abortNode) {
        //
    }

    @Override
    public void visit(BLangRetry retryNode) {

    }

    @Override
    public void visit(BLangContinue continueNode) {

    }

    @Override
    public void visit(BLangBreak breakNode) {

    }

    @Override
    public void visit(BLangReturn returnNode) {
        this.acceptNode(returnNode.expr);
    }

    @Override
    public void visit(BLangThrow throwNode) {

    }

    @Override
    public void visit(BLangPanic panicNode) {
        this.acceptNode(panicNode.expr);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {

    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        this.acceptNode(exprStmtNode.expr);
    }

    @Override
    public void visit(BLangIf ifNode) {
        // Visit the expression
        this.acceptNode(ifNode.expr);
        // Visit the body
        this.acceptNode(ifNode.body);
        if (ifNode.elseStmt != null) {
            this.acceptNode(ifNode.elseStmt);
        }
    }

    @Override
    public void visit(BLangMatch matchNode) {
        this.acceptNode(matchNode.expr);
        matchNode.patternClauses.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {

    }

    @Override
    public void visit(BLangForeach foreach) {
        this.acceptNode(foreach.collection);
        this.acceptNode((BLangNode) foreach.variableDefinitionNode);
        this.acceptNode(foreach.body);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        this.acceptNode(whileNode.expr);
        this.acceptNode(whileNode.body);
    }

    @Override
    public void visit(BLangLock lockNode) {
        this.acceptNode(lockNode.body);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        this.acceptNode(transactionNode.retryCount);
        this.acceptNode(transactionNode.transactionBody);
        this.acceptNode(transactionNode.onRetryBody);
        this.acceptNode(transactionNode.committedBody);
        this.acceptNode(transactionNode.abortedBody);
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {

    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        stmt.varRef.expressions.forEach(this::acceptNode);
        this.acceptNode(stmt.expr);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        this.acceptNode(stmt.varRef);
        this.acceptNode(stmt.expr);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {

    }

    @Override
    public void visit(BLangCatch catchNode) {

    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangOrderBy orderBy) {

    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {

    }

    @Override
    public void visit(BLangLimit limit) {

    }

    @Override
    public void visit(BLangGroupBy groupBy) {

    }

    @Override
    public void visit(BLangHaving having) {

    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {

    }

    @Override
    public void visit(BLangSelectClause selectClause) {

    }

    @Override
    public void visit(BLangWhere whereClause) {

    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {

    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {

    }

    @Override
    public void visit(BLangTableQuery tableQuery) {

    }

    @Override
    public void visit(BLangStreamAction streamAction) {

    }

    @Override
    public void visit(BLangFunctionClause functionClause) {

    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {

    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {

    }

    @Override
    public void visit(BLangWindow windowClause) {

    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {

    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        this.acceptNode(workerSendNode.expr);
        //TODO:COMPLETE
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        //TODO:COMPLETE
    }

    @Override
    public void visit(BLangForever foreverStatement) {

    }

    @Override
    public void visit(BLangLiteral literalExpr) {

    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {

    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {

    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        recordLiteral.keyValuePairs.forEach(bLangRecordKeyValue -> this.acceptNode(bLangRecordKeyValue.valueExpr));
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        varRefExpr.expressions.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        varRefExpr.recordRefFields.forEach(varRefKeyVal -> this.acceptNode(varRefKeyVal.variableReference));
        if (varRefExpr.restParam instanceof BLangSimpleVarRef) {
            this.acceptNode((BLangSimpleVarRef) varRefExpr.restParam);
        }
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {

    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        addReference((BVarSymbol) varRefExpr.symbol, varRefExpr.pos, varRefExpr.getWS());
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        this.acceptNode(fieldAccessExpr.expr);
        addReference(fieldAccessExpr.varSymbol, fieldAccessExpr.pos, fieldAccessExpr.getWS());
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        this.acceptNode(indexAccessExpr.expr);
        if (!(indexAccessExpr.indexExpr instanceof BLangLiteral)) {
            // Visit the index expression only if it's not a simple literal since there is no use otherwise
            this.acceptNode(indexAccessExpr.indexExpr);
        }
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (invocationExpr.expr != null)
            this.acceptNode(invocationExpr.expr);
        addReference((BVarSymbol) invocationExpr.symbol, invocationExpr.pos, invocationExpr.getWS());
        invocationExpr.argExprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangTypeInit typeInit) {
        addReference(typeInit.initInvocation.symbol, typeInit.pos, typeInit.getWS());
        if (typeInit.userDefinedType != null) {
            this.acceptNode(typeInit.userDefinedType);
        }
        typeInit.argsExpr.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {

    }

    @Override
    public void visit(BLangInvocation.BLangBuiltInMethodInvocation builtInMethodInvocation) {

    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        this.acceptNode(ternaryExpr.expr);
        this.acceptNode(ternaryExpr.thenExpr);
        this.acceptNode(ternaryExpr.elseExpr);
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        awaitExpr.exprList.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {

    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        this.acceptNode(binaryExpr.lhsExpr);
        this.acceptNode(binaryExpr.rhsExpr);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {

    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        this.acceptNode(groupExpr.expression);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        listConstructorExpr.exprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {

    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {

    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        this.acceptNode(unaryExpr.expr);
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {

    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        this.acceptNode(conversionExpr.expr);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {

    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {

    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {

    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {

    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {

    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {

    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {

    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {

    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        BLangFunction funcNode = bLangLambdaFunction.function;
        funcNode.annAttachments.forEach(this::acceptNode);
        funcNode.requiredParams.forEach(this::acceptNode);
        funcNode.externalAnnAttachments.forEach(this::acceptNode);
        funcNode.returnTypeAnnAttachments.forEach(this::acceptNode);
        this.acceptNode(funcNode.returnTypeNode);
        this.acceptNode(funcNode.body);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {

    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        this.acceptNode(xmlAttributeAccessExpr.expr);
        this.acceptNode(xmlAttributeAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {

    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {

    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {

    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        this.acceptNode(bLangNamedArgsExpression.expr);
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {

    }

    @Override
    public void visit(BLangWithinClause withinClause) {

    }

    @Override
    public void visit(BLangOutputRateLimit outputRateLimit) {

    }

    @Override
    public void visit(BLangPatternClause patternClause) {

    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {

    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {

    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {

    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        this.acceptNode(checkedExpr.expr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        this.acceptNode(checkPanickedExpr.expr);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {

    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        this.acceptNode(typeTestExpr.expr);
        this.acceptNode(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {

    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {

    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        this.acceptNode(annotAccessExpr.expr);
    }

    @Override
    public void visit(BLangValueType valueType) {

    }

    @Override
    public void visit(BLangArrayType arrayType) {
        this.acceptNode(arrayType.elemtype);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {

    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        this.acceptNode(constrainedType.type);
        this.acceptNode(constrainedType.constraint);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        addReference(userDefinedType.type.tsymbol, userDefinedType.pos, userDefinedType.getWS());
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {

    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        unionTypeNode.getMemberTypeNodes().forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        objectTypeNode.typeRefs.forEach(this::addObjectReferenceType);
        objectTypeNode.fields.forEach(this::acceptNode);
        objectTypeNode.functions.forEach(this::acceptNode);
        this.acceptNode(objectTypeNode.initFunction);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        recordTypeNode.fields.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {

    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {

    }

    @Override
    public void visit(BLangErrorType errorType) {

    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {

    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {

    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {

    }

    @Override
    public void visit(BLangSimpleVarRef.BLangConstRef constRef) {

    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {

    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {

    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {

    }

    @Override
    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {

    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {

    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {

    }

    @Override
    public void visit(BLangRecordLiteral.BLangStreamLiteral streamLiteral) {

    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {

    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {

    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {

    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {

    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {

    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {

    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {

    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {

    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {

    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {

    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {

    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {

    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        bLangTupleVariable.memberVariables.forEach(this::acceptNode);
        this.acceptNode(bLangTupleVariable.typeNode);
        this.acceptNode(bLangTupleVariable.expr);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        this.acceptNode(bLangTupleVariableDef.var);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        bLangRecordVariable.variableList
                .forEach(variableKeyValue -> this.acceptNode(variableKeyValue.valueBindingPattern));
        this.acceptNode(bLangRecordVariable.typeNode);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        this.acceptNode(bLangRecordVariableDef.var);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {

    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {

    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause bLangMatchStmtStaticBindingPatternClause) {
        this.acceptNode(bLangMatchStmtStaticBindingPatternClause.body);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause structuredBindingPatternClause) {
        this.acceptNode(structuredBindingPatternClause.bindingPatternVariable);
        this.acceptNode(structuredBindingPatternClause.body);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {

    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        this.acceptNode(syncSendExpr.expr);
        //TODO:COMPLETE
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {

    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {

    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValue recordKeyValue) {

    }

    private void addObjectReferenceType(BLangType bLangType) {
        if (!(bLangType instanceof BLangUserDefinedType)
                || !(bLangType.type instanceof BObjectType)) {
            return;
        }
        BObjectType objectType = (BObjectType) bLangType.type;
        addReference(objectType.tsymbol, bLangType.pos, bLangType.getWS());
    }
}

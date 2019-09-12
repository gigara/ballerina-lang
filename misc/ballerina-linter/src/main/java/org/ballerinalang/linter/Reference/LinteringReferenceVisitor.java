package org.ballerinalang.linter.Reference;

import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.*;
import org.wso2.ballerinalang.compiler.tree.clauses.*;
import org.wso2.ballerinalang.compiler.tree.expressions.*;
import org.wso2.ballerinalang.compiler.tree.statements.*;
import org.wso2.ballerinalang.compiler.tree.types.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LinteringReferenceVisitor extends BLangNodeVisitor {
    private List<Definition> definitions = new ArrayList<>();
    private List<Reference> references = new ArrayList<>();

    public LinteringReferenceVisitor() {
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public List<Reference> getReferences() {
        return references;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        super.visit(pkgNode);
    }

    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
        super.visit(testablePkgNode);
    }


    @Override
    public void visit(BLangCompilationUnit compUnit) {
        List<TopLevelNode> topLevelNodes = compUnit.topLevelNodes;
        topLevelNodes.forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
    }

    private void acceptNode(BLangNode node) {
        node.accept(this);
    }

    // add definition to the arrayList
    private void addDefinition(BVarSymbol symbol, Diagnostic.DiagnosticPosition pos, Set<Whitespace> ws) {
        Symbol symbol1 = new Symbol(symbol.name.value, symbol.kind != null ? symbol.kind.name() : symbol.type.tsymbol.name.value,
                symbol.pkgID.name.value, symbol.pkgID.orgName.value);
        if (!availableInDefinitions(symbol1))
            definitions.add(new Definition(symbol1, availableInReferences(symbol1), position(pos, ws, symbol.name.value)));
        for (Reference reference : references) {
            if (symbol1.equals(reference.getSymbol()))
                if (!reference.isHasDefinition()) {
                    reference.setHasDefinition(true);
                }
        }
    }

    // add reference to the arrayList
    private void addReference(BVarSymbol symbol) {
        Symbol symbol1 = new Symbol(symbol.name.value, symbol.kind != null ? symbol.kind.name() : symbol.type.tsymbol.name.value,
                symbol.pkgID.name.value, symbol.pkgID.orgName.value);
        if (!availableInReferences(symbol1)) {
            boolean availableInDef = false;
            for (Definition definition1 : definitions) {
                if (symbol1.equals(definition1.getSymbol())) {
                    availableInDef = true;

                    if (!definition1.isHasReference())
                        definition1.setHasReference(true);
                }
            }
            references.add(new Reference(symbol1, availableInDef));

        } else {
            for (Reference reference : references) {
                if (symbol1.equals(reference.getSymbol()))
                    reference.setCount(reference.getCount() + 1);
            }
        }
    }

    // search for the symbol in references list
    private boolean availableInReferences(Symbol symbol) {
        for (Reference reference : references) {
            if (symbol.equals(reference.getSymbol()))
                return true;
        }
        return false;
    }

    // search for the symbol in definitions list
    private boolean availableInDefinitions(Symbol symbol) {
        for (Definition definition1 : definitions) {
            if (symbol.equals(definition1.getSymbol()))
                return true;
        }
        return false;
    }

    // diagnostic log end position
    private Diagnostic.DiagnosticPosition position(Diagnostic.DiagnosticPosition position, Set<Whitespace> ws, String text) {
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
    public void visit(BLangImportPackage importPkgNode) {
        //TODO: Complete
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        super.visit(xmlnsNode);
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
        super.visit(serviceNode);
    }

    @Override
    public void visit(BLangResource resourceNode) {
        super.visit(resourceNode);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        super.visit(typeDefinition);
    }

    @Override
    public void visit(BLangConstant constant) {
        super.visit(constant);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        BLangType typeNode = varNode.typeNode;
        if (varNode.flagSet.contains(Flag.SERVICE)) {
            // Skip the anon service symbol generated for the BLangService,
            // which will be visited from BLangService visitor
            return;
        }
        varNode.annAttachments.forEach(this::acceptNode);
        this.acceptNode(varNode.expr);
    }

    @Override
    public void visit(BLangWorker workerNode) {
        super.visit(workerNode);
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        super.visit(endpointNode);
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        super.visit(identifierNode);
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        //
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        this.acceptNode(annAttachmentNode.expr);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.getStatements().forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
        super.visit(lockStmtNode);
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
        super.visit(unLockNode);
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
        super.visit(compoundAssignNode);
    }

    @Override
    public void visit(BLangAbort abortNode) {
        super.visit(abortNode);
    }

    @Override
    public void visit(BLangRetry retryNode) {
        super.visit(retryNode);
    }

    @Override
    public void visit(BLangContinue continueNode) {
        super.visit(continueNode);
    }

    @Override
    public void visit(BLangBreak breakNode) {
        super.visit(breakNode);
    }

    @Override
    public void visit(BLangReturn returnNode) {
        super.visit(returnNode);
    }

    @Override
    public void visit(BLangThrow throwNode) {
        super.visit(throwNode);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        super.visit(panicNode);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        super.visit(xmlnsStmtNode);
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
        super.visit(matchNode);
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        super.visit(patternClauseNode);
    }

    @Override
    public void visit(BLangForeach foreach) {
        super.visit(foreach);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        super.visit(whileNode);
    }

    @Override
    public void visit(BLangLock lockNode) {
        super.visit(lockNode);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        super.visit(transactionNode);
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        super.visit(tryNode);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        super.visit(stmt);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        super.visit(stmt);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        super.visit(stmt);
    }

    @Override
    public void visit(BLangCatch catchNode) {
        super.visit(catchNode);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        super.visit(forkJoin);
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        super.visit(orderBy);
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
        super.visit(orderByVariable);
    }

    @Override
    public void visit(BLangLimit limit) {
        super.visit(limit);
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        super.visit(groupBy);
    }

    @Override
    public void visit(BLangHaving having) {
        super.visit(having);
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        super.visit(selectExpression);
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        super.visit(selectClause);
    }

    @Override
    public void visit(BLangWhere whereClause) {
        super.visit(whereClause);
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        super.visit(streamingInput);
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        super.visit(joinStreamingInput);
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {
        super.visit(tableQuery);
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        super.visit(streamAction);
    }

    @Override
    public void visit(BLangFunctionClause functionClause) {
        super.visit(functionClause);
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        super.visit(setAssignmentClause);
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
        super.visit(patternStreamingEdgeInput);
    }

    @Override
    public void visit(BLangWindow windowClause) {
        super.visit(windowClause);
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
        super.visit(patternStreamingInput);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        super.visit(workerSendNode);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        super.visit(workerReceiveNode);
    }

    @Override
    public void visit(BLangForever foreverStatement) {
        super.visit(foreverStatement);
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        //TODO:COMPLETE
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        //TODO:COMPLETE
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        super.visit(tableLiteral);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        super.visit(recordLiteral);
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        super.visit(varRefExpr);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        super.visit(varRefExpr);
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        super.visit(varRefExpr);
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        addReference((BVarSymbol) varRefExpr.symbol);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        super.visit(fieldAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        super.visit(indexAccessExpr);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (invocationExpr.expr != null)
            this.acceptNode(invocationExpr.expr);
        addReference((BVarSymbol) invocationExpr.symbol);
        invocationExpr.argExprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
        super.visit(connectorInitExpr);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        super.visit(actionInvocationExpr);
    }

    @Override
    public void visit(BLangInvocation.BLangBuiltInMethodInvocation builtInMethodInvocation) {
        super.visit(builtInMethodInvocation);
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        super.visit(ternaryExpr);
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        super.visit(awaitExpr);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        super.visit(trapExpr);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        super.visit(binaryExpr);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        super.visit(elvisExpr);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        super.visit(groupExpr);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        super.visit(listConstructorExpr);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        super.visit(tupleLiteral);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        super.visit(arrayLiteral);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        super.visit(unaryExpr);
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        super.visit(accessExpr);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        super.visit(conversionExpr);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        super.visit(xmlQName);
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        super.visit(xmlAttribute);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        super.visit(xmlElementLiteral);
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        super.visit(xmlTextLiteral);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        super.visit(xmlCommentLiteral);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        super.visit(xmlProcInsLiteral);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        super.visit(xmlQuotedString);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        super.visit(stringTemplateLiteral);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        super.visit(bLangLambdaFunction);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        super.visit(bLangArrowFunction);
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        super.visit(xmlAttributeAccessExpr);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        super.visit(intRangeExpression);
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        super.visit(tableQueryExpression);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        super.visit(bLangVarArgsExpression);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        super.visit(bLangNamedArgsExpression);
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        super.visit(streamingQueryStatement);
    }

    @Override
    public void visit(BLangWithinClause withinClause) {
        super.visit(withinClause);
    }

    @Override
    public void visit(BLangOutputRateLimit outputRateLimit) {
        super.visit(outputRateLimit);
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
        super.visit(patternClause);
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        super.visit(assignableExpr);
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        super.visit(bLangMatchExpression);
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        super.visit(bLangMatchExprPatternClause);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        super.visit(checkedExpr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        super.visit(checkPanickedExpr);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        super.visit(serviceConstructorExpr);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        super.visit(typeTestExpr);
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        super.visit(typeTestExpr);
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        super.visit(ignoreExpr);
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        super.visit(annotAccessExpr);
    }

    @Override
    public void visit(BLangValueType valueType) {
        //TODO:COMPLETE
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        super.visit(arrayType);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        super.visit(builtInRefType);
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        super.visit(constrainedType);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        super.visit(userDefinedType);
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        super.visit(functionTypeNode);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        super.visit(unionTypeNode);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        super.visit(objectTypeNode);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        super.visit(recordTypeNode);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        super.visit(finiteTypeNode);
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        super.visit(tupleTypeNode);
    }

    @Override
    public void visit(BLangErrorType errorType) {
        super.visit(errorType);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        super.visit(localVarRef);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        super.visit(fieldVarRef);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        super.visit(packageVarRef);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangConstRef constRef) {
        super.visit(constRef);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        super.visit(functionVarRef);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        super.visit(typeLoad);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        super.visit(fieldAccessExpr);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        super.visit(functionVarRef);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        super.visit(mapKeyAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        super.visit(arrayIndexAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {
        super.visit(arrayIndexAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {
        super.visit(xmlAccessExpr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {
        super.visit(jsonLiteral);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        super.visit(mapLiteral);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        super.visit(structLiteral);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStreamLiteral streamLiteral) {
        super.visit(streamLiteral);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {
        super.visit(channelLiteral);
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        super.visit(bFunctionPointerInvocation);
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        super.visit(iExpr);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        super.visit(jsonArrayLiteral);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        super.visit(jsonAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        super.visit(stringAccessExpr);
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        super.visit(xmlnsNode);
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        super.visit(xmlnsNode);
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        super.visit(bLangXMLSequenceLiteral);
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        super.visit(bLangStatementExpression);
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {
        super.visit(bLangMarkdownDocumentationLine);
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {
        super.visit(bLangDocumentationParameter);
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {
        super.visit(bLangMarkdownReturnParameterDocumentation);
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
        super.visit(bLangMarkdownDocumentation);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        super.visit(bLangTupleVariable);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        super.visit(bLangTupleVariableDef);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        super.visit(bLangRecordVariable);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        super.visit(bLangRecordVariableDef);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        super.visit(bLangErrorVariable);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        super.visit(bLangErrorVariableDef);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause bLangMatchStmtStaticBindingPatternClause) {
        super.visit(bLangMatchStmtStaticBindingPatternClause);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause bLangMatchStmtStructuredBindingPatternClause) {
        super.visit(bLangMatchStmtStructuredBindingPatternClause);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        super.visit(workerFlushExpr);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        super.visit(syncSendExpr);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        super.visit(waitForAllExpr);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        super.visit(waitLiteral);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValue recordKeyValue) {
        super.visit(recordKeyValue);
    }
}

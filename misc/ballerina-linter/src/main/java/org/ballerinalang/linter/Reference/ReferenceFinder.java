/*

 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 */

package org.ballerinalang.linter.Reference;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFunctionClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimit;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOutputRateLimit;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingEdgeInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSetAssignment;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangTableQuery;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWindow;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWithinClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.HashMap;
import java.util.List;

/**
 * Ballerina linter reference finder
 *
 * @since 1.0.1
 */
public class ReferenceFinder extends BLangNodeVisitor {
    private HashMap<String, Definition> definitions = new HashMap<>();

    /**
     * return the list of definitions as a HashMap
     *
     * @return definitions
     */
    public HashMap<String, Definition> getDefinitions() {
        return definitions;
    }

    private void acceptNode(BLangNode node) {
        node.accept(this);
    }

    // add definition to the arrayList
    private void addDefinition(BSymbol symbol, Diagnostic.DiagnosticPosition pos) {
        Definition definition = new Definition(symbol, false, true, pos);
        if (!availableInDefinitions(definition)) {
            definitions.put(definition.md5(), definition);
        } else {
            definitions.get(definition.md5()).setHasDefinition(true);
        }
    }

    // add reference to the arrayList
    private void addReference(BSymbol symbol, Diagnostic.DiagnosticPosition pos) {
        Definition definition = new Definition(symbol, true, false, pos);
        if (availableInDefinitions(definition)) {
            definitions.get(definition.md5()).setHasReference(true);
        } else {
            definitions.put(definition.md5(), definition);
        }
    }

    // search for the symbol in definitions list
    private boolean availableInDefinitions(Definition definition) {
        return definitions.containsKey(definition.md5());
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        List<TopLevelNode> topLevelNodes = compUnit.topLevelNodes;
        topLevelNodes.forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        addDefinition(xmlnsNode.symbol, xmlnsNode.namespaceURI.pos);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        if (!funcNode.name.value.equals("main")) {
            addDefinition(funcNode.symbol, funcNode.name.pos);
        }
        funcNode.annAttachments.forEach(this::acceptNode);
        funcNode.requiredParams.forEach(this::acceptNode);
        funcNode.externalAnnAttachments.forEach(this::acceptNode);
        funcNode.returnTypeAnnAttachments.forEach(this::acceptNode);
        this.acceptNode(funcNode.returnTypeNode);
        this.acceptNode(funcNode.body);

    }

    @Override
    public void visit(BLangService serviceNode) {
        addDefinition(serviceNode.symbol, serviceNode.name.pos);
        serviceNode.annAttachments.forEach(this::acceptNode);
        if (serviceNode.attachedExprs != null) {
            serviceNode.attachedExprs.forEach(this::acceptNode);
        }
        serviceNode.resourceFunctions.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangResource resourceNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        addDefinition(typeDefinition.symbol, typeDefinition.name.pos);
        typeDefinition.annAttachments.forEach(this::acceptNode);
        // Visit the type node
        this.acceptNode(typeDefinition.typeNode);
    }

    @Override
    public void visit(BLangConstant constant) {
        addDefinition(constant.symbol, constant.name.pos);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        if (varNode.flagSet.contains(Flag.SERVICE)) {
            // Skip the anon service symbol generated for the BLangService,
            // which will be visited from BLangService visitor
            return;
        }
        addDefinition(varNode.symbol, varNode.name.pos);
        varNode.annAttachments.forEach(this::acceptNode);
        this.acceptNode(varNode.expr);
    }

    @Override
    public void visit(BLangWorker workerNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        addDefinition(annotationNode.symbol, annotationNode.name.pos);
        annotationNode.annAttachments.forEach(this::acceptNode);
        this.acceptNode(annotationNode.typeNode);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        this.acceptNode(annAttachmentNode.expr);
        addReference(annAttachmentNode.annotationSymbol, annAttachmentNode.annotationName.pos);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.getStatements().forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        BLangSimpleVariable variable = varDefNode.var;
        addDefinition(variable.symbol, varDefNode.var.name.pos);

        BLangType typeNode = variable.typeNode;
        if (varDefNode.getWS() == null) {
            if (varDefNode.var.expr instanceof BLangLambdaFunction
                    && ((BLangLambdaFunction) varDefNode.var.expr).function.flagSet.contains(Flag.WORKER)) {
                return;
            }
        } else {
            // In the foreach's variable definition node, type becomes null and will be handled by the acceptNode
            if (typeNode != null) {
                this.acceptNode(typeNode);
            }
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
        // No implementation needed.
    }

    @Override
    public void visit(BLangRetry retryNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangContinue continueNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangBreak breakNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangReturn returnNode) {
        this.acceptNode(returnNode.expr);
    }

    @Override
    public void visit(BLangThrow throwNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangPanic panicNode) {
        this.acceptNode(panicNode.expr);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        // No implementation needed.
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
        // No implementation needed.
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
        // No implementation needed.
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
        // No implementation needed.
    }

    @Override
    public void visit(BLangCatch catchNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangLimit limit) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangHaving having) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangWhere whereClause) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangFunctionClause functionClause) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangWindow windowClause) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
        // No implementation needed.
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
        // No implementation needed.
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        // No implementation needed.
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
        // No implementation needed.
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        addReference((BVarSymbol) varRefExpr.symbol, varRefExpr.variableName.pos);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        this.acceptNode(fieldAccessExpr.expr);
        addReference(fieldAccessExpr.varSymbol, fieldAccessExpr.field.pos);
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
        if (invocationExpr.expr != null) {
            this.acceptNode(invocationExpr.expr);
        }
        addReference((BVarSymbol) invocationExpr.symbol, invocationExpr.name.pos);
        invocationExpr.argExprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangTypeInit typeInit) {
        addReference(typeInit.initInvocation.symbol, typeInit.userDefinedType.typeName.pos);
        if (typeInit.userDefinedType != null) {
            this.acceptNode(typeInit.userDefinedType);
        }
        typeInit.argsExpr.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangInvocation.BLangBuiltInMethodInvocation builtInMethodInvocation) {
        // No implementation needed.
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
        // No implementation needed.
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        this.acceptNode(binaryExpr.lhsExpr);
        this.acceptNode(binaryExpr.rhsExpr);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        // No implementation needed.
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
        // No implementation needed.
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        this.acceptNode(unaryExpr.expr);
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        this.acceptNode(conversionExpr.expr);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        // No implementation needed.
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
        // No implementation needed.
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        this.acceptNode(xmlAttributeAccessExpr.expr);
        this.acceptNode(xmlAttributeAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        this.acceptNode(bLangNamedArgsExpression.expr);
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangWithinClause withinClause) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangOutputRateLimit outputRateLimit) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        // No implementation needed.
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
        // No implementation needed.
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        this.acceptNode(typeTestExpr.expr);
        this.acceptNode(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        this.acceptNode(annotAccessExpr.expr);
    }

    @Override
    public void visit(BLangValueType valueType) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        this.acceptNode(arrayType.elemtype);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        this.acceptNode(constrainedType.type);
        this.acceptNode(constrainedType.constraint);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        addReference(userDefinedType.type.tsymbol, userDefinedType.typeName.pos);
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        // No implementation needed.
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
        // No implementation needed.
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangErrorType errorType) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangConstRef constRef) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStreamLiteral streamLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
        // No implementation needed.
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
        // No implementation needed.
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        // No implementation needed.
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
        // No implementation needed.
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        this.acceptNode(syncSendExpr.expr);
        //TODO:COMPLETE
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        // No implementation needed.
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValue recordKeyValue) {
        // No implementation needed.
    }

    private void addObjectReferenceType(BLangType bLangType) {
        if (!(bLangType instanceof BLangUserDefinedType)
                || !(bLangType.type instanceof BObjectType)) {
            return;
        }
        BObjectType objectType = (BObjectType) bLangType.type;
        addReference(objectType.tsymbol, bLangType.pos);
    }
}

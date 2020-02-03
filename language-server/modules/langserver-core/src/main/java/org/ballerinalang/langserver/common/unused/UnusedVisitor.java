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
package org.ballerinalang.langserver.common.unused;

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Linter code action unused variable visitor.
 *
 * @since 1.2.0
 */
public class UnusedVisitor extends LSNodeVisitor {
    private Position position;
    private boolean terminateVisitor;
    private DiagnosticPos diagnosticPos;
    private LSContext context;

    public UnusedVisitor(Position position, LSContext context) {
        this.position = position;
        this.context = context;
    }

    public DiagnosticPos getDiagnosticPos() {
        return diagnosticPos;
    }

    /**
     * Accept node to visit.
     *
     * @param node node to be accepted to visit.
     */
    private void acceptNode(BLangNode node) {
        if (this.terminateVisitor || node == null) {
            return;
        }
        node.accept(this);
    }

    private boolean isMatching(DiagnosticPos namePosition) {
        boolean isMatching = false;

        if (namePosition.sLine == position.getLine()
                && namePosition.eLine >= position.getLine()
                && namePosition.sCol <= position.getCharacter()
                && namePosition.eCol >= position.getCharacter()) {
            isMatching = true;
        }

        return isMatching;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        boolean isTestSrc = CommonUtil.isTestSource(this.context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY));
        BLangPackage evalPkg = isTestSrc ? pkgNode.getTestablePkg() : pkgNode;
        List<TopLevelNode> topLevelNodes = CommonUtil.getCurrentFileTopLevelNodes(evalPkg, this.context);
        topLevelNodes.stream()
                .filter(CommonUtil.checkInvalidTypesDefs())
                .forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        super.visit(compUnit);
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        super.visit(importPkgNode);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        // Check for native functions
        if (isMatching(funcNode.name.pos)) {
            setTerminateVisitor();
            diagnosticPos = funcNode.pos;
        }

        if (funcNode.requiredParams != null && !terminateVisitor) {
            funcNode.requiredParams.forEach(this::acceptNode);
        }

        if (funcNode.returnTypeNode != null && !terminateVisitor &&
                !(funcNode.returnTypeNode.type instanceof BNilType)) {
            this.acceptNode(funcNode.returnTypeNode);
        }

        if (funcNode.body != null && !terminateVisitor) {
            this.acceptNode(funcNode.body);
        }

        // Process workers
        if (funcNode.workers != null && !terminateVisitor) {
            funcNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        if (varDefNode.getVariable() != null && !terminateVisitor) {
            this.acceptNode(varDefNode.getVariable());
        }
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        boolean isFunctionParam = varNode.parent instanceof BLangFunction;
        boolean isTupleVariable = varNode.parent instanceof BLangTupleVariable;
        boolean isRecordVariable = varNode.parent instanceof BLangRecordVariable;

        if (varNode.symbol != null && !terminateVisitor) {
            if (isMatching(varNode.name.pos)) {
                setTerminateVisitor();
                diagnosticPos = varNode.pos;

                // change columns if the var is a function parameter
                if (isFunctionParam || isTupleVariable || isRecordVariable) {
                    List<? extends BLangVariable> params = null;

                    if (isFunctionParam) {
                        params = ((BLangFunction) varNode.parent).requiredParams;

                    } else if (isTupleVariable) {
                        params = ((BLangTupleVariable) varNode.parent).memberVariables;

                    } else if (isRecordVariable) {
                        List<BLangVariable> variables = new ArrayList<>();
                        for (BLangRecordVariable.BLangRecordVariableKeyValue variable:
                                ((BLangRecordVariable) varNode.parent).variableList) {
                            variables.add(variable.valueBindingPattern);
                        }
                        params = variables;
                    }

                    if (params != null) {
                        for (int i = 0; i < params.size(); i++) {
                            if (params.get(i) == varNode) {
                                if (params.size() > 1) {
                                    if (i != params.size() - 1) {
                                        DiagnosticPos nextNodePos = params.get(i + 1).pos;
                                        diagnosticPos.eCol = nextNodePos.sCol;

                                    } else {
                                        DiagnosticPos previousNodePos = params.get(i - 1).pos;
                                        diagnosticPos.sCol = previousNodePos.eCol;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (varNode.expr != null) {
            this.acceptNode(varNode.expr);
        }

        if (varNode.getTypeNode() != null) {
            this.acceptNode(varNode.getTypeNode());
        }
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        if (blockNode.stmts != null && !terminateVisitor) {
            blockNode.stmts.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangConstant constant) {
        if (constant.symbol != null && !terminateVisitor) {
            if (isMatching(constant.name.pos)) {
                setTerminateVisitor();
                diagnosticPos = constant.pos;

            }
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.symbol != null && !terminateVisitor) {
            if (isMatching(typeDefinition.name.pos)) {
                setTerminateVisitor();
                diagnosticPos = typeDefinition.pos;
                return;
            }
        }

        if (typeDefinition.typeNode != null && !terminateVisitor) {
            this.acceptNode(typeDefinition.typeNode);
        }
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        if (recordTypeNode.fields != null && !terminateVisitor) {
            recordTypeNode.fields.forEach(this::acceptNode);
        }

        if (recordTypeNode.initFunction != null  && !terminateVisitor &&
                !(recordTypeNode.initFunction.returnTypeNode.type instanceof BNilType)) {
            this.acceptNode(recordTypeNode.initFunction);
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {

        if (objectTypeNode.fields != null && !terminateVisitor) {
            objectTypeNode.fields.forEach(this::acceptNode);
        }

        if (objectTypeNode.functions != null && !terminateVisitor) {
            objectTypeNode.functions.forEach(this::acceptNode);
        }

        if (objectTypeNode.initFunction != null && !terminateVisitor) {
            this.acceptNode(objectTypeNode.initFunction);
        }

        if (objectTypeNode.receiver != null && !terminateVisitor) {
            this.acceptNode(objectTypeNode.receiver);
        }
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        if (serviceConstructorExpr.serviceNode != null && !terminateVisitor) {
            this.acceptNode(serviceConstructorExpr.serviceNode);
        }
    }

    @Override
    public void visit(BLangService serviceNode) {
        if (serviceNode.resourceFunctions != null && !terminateVisitor) {
            serviceNode.resourceFunctions.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        if (bLangTupleVariableDef.var != null && !terminateVisitor) {
            this.acceptNode(bLangTupleVariableDef.var);
        }
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        if (bLangTupleVariable.memberVariables != null && !terminateVisitor) {
            bLangTupleVariable.memberVariables.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangWhile whileNode) {
        if (whileNode.expr != null && !terminateVisitor) {
            this.acceptNode(whileNode.expr);
        }
        if (whileNode.body != null && !terminateVisitor) {
            this.acceptNode(whileNode.body);
        }
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        if (groupExpr.expression != null && !terminateVisitor) {
            this.acceptNode(groupExpr.expression);
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        if (transactionNode.transactionBody != null && !terminateVisitor) {
            acceptNode(transactionNode.transactionBody);
        }

        if (transactionNode.onRetryBody != null && !terminateVisitor) {
            acceptNode(transactionNode.onRetryBody);
        }

        if (transactionNode.retryCount != null && !terminateVisitor) {
            acceptNode(transactionNode.retryCount);
        }
    }

    @Override
    public void visit(BLangIf ifNode) {
        if (ifNode.expr != null && !terminateVisitor) {
            acceptNode(ifNode.expr);
        }

        if (ifNode.body != null && !terminateVisitor) {
            acceptNode(ifNode.body);
        }

        if (ifNode.elseStmt != null && !terminateVisitor) {
            acceptNode(ifNode.elseStmt);
        }
    }

    public void visit(BLangForeach foreach) {
        if (foreach.collection != null && !terminateVisitor) {
            acceptNode(foreach.collection);
        }

        acceptNode((BLangNode) foreach.variableDefinitionNode);

        if (foreach.body != null && !terminateVisitor) {
            acceptNode(foreach.body);
        }
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        if (bLangRecordVariable != null) {
            for (BLangRecordVariable.BLangRecordVariableKeyValue bLangRecordVariableKeyValue :
                    bLangRecordVariable.variableList) {
                acceptNode(bLangRecordVariableKeyValue.valueBindingPattern);
            }
            acceptNode(bLangRecordVariable.expr);
        }
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        if (bLangRecordVariableDef.var != null && !terminateVisitor) {
            acceptNode(bLangRecordVariableDef.var);
        }
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        if (stmt.expr != null) {
            acceptNode(stmt.expr);
        }

        if (stmt.varRef != null) {
            acceptNode(stmt.varRef);
        }
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (invocationExpr.expr != null && !terminateVisitor) {
            acceptNode(invocationExpr.expr);
        }

        if (invocationExpr.argExprs != null && !terminateVisitor) {
            invocationExpr.argExprs.forEach(this::acceptNode);
        }

        if (!terminateVisitor && HoverUtil.isMatchingPosition(invocationExpr.getPosition(), this.position)) {
            setTerminateVisitor();
        }
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        if (varRefExpr.restParam != null && !terminateVisitor) {
            acceptNode((BLangNode) varRefExpr.restParam);
        }
    }

    /**
     * Set terminate visitor.
     */
    private void setTerminateVisitor() {
        this.terminateVisitor = true;
    }
}

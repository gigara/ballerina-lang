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
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;

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
        BSymbol funcSymbol = funcNode.symbol;
        if (Symbols.isNative(funcSymbol) || !CommonUtil.isValidInvokableSymbol(funcSymbol)) {
            return;
        }

        if (HoverUtil.isMatchingPosition(funcNode.pos, this.position)) {
            setTerminateVisitor();
            diagnosticPos = funcNode.pos;
        }

        if (funcNode.requiredParams != null && !terminateVisitor) {
            funcNode.requiredParams.forEach(this::acceptNode);
        }

        if (funcNode.returnTypeNode != null && !terminateVisitor && !(funcNode.returnTypeNode.type instanceof BNilType)) {
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
        if (varNode.symbol != null && !terminateVisitor) {
            if (HoverUtil.isMatchingPosition(varNode.pos, this.position)) {
                setTerminateVisitor();
                diagnosticPos = varNode.pos;
            }
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
            if (HoverUtil.isMatchingPosition(constant.pos, this.position)) {
                setTerminateVisitor();
                diagnosticPos = constant.pos;
            }
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.symbol != null && !terminateVisitor) {
            if (HoverUtil.isMatchingPosition(typeDefinition.pos, this.position)) {
                setTerminateVisitor();
                diagnosticPos = typeDefinition.pos;
            }
        }

        if (typeDefinition.typeNode != null && !terminateVisitor) {
            this.acceptNode(typeDefinition.typeNode);
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {

        if (objectTypeNode.fields != null) {
            objectTypeNode.fields.forEach(this::acceptNode);
        }

        if (objectTypeNode.functions != null) {
            objectTypeNode.functions.forEach(this::acceptNode);
        }

        if (objectTypeNode.initFunction != null) {
            this.acceptNode(objectTypeNode.initFunction);
        }

        if (objectTypeNode.receiver != null) {
            this.acceptNode(objectTypeNode.receiver);
        }
    }

    /**
     * Set terminate visitor.
     */
    private void setTerminateVisitor() {
        this.terminateVisitor = true;
    }
}

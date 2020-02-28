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

package org.ballerinalang.langserver.codeaction.providers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.command.executors.UnusedVariableExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.util.references.ReferencesUtil.getReferenceAtCursor;

/**
 * Code Action provider for remove unused variables.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class RemoveUnusedVariableCodeAction extends AbstractCodeActionProvider {
    private static final String NEVER_USED = " is never used";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> allDiagnostics) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> diagnosticsOfRange,
                                                    List<Diagnostic> allDiagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        WorkspaceDocumentManager documentManager = lsContext.get(LSGlobalContextKeys.DOCUMENT_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(lsContext.get(DocumentServiceKeys.FILE_URI_KEY));
        LSDocumentIdentifier document = null;
        try {
            document = documentManager.getLSDocument(filePath.get());
        } catch (WorkspaceDocumentException e) {
            // ignore
        }
        for (Diagnostic diagnostic : allDiagnostics) {
            if (diagnostic.getMessage().endsWith(NEVER_USED)) {
                actions.add(getRemoveUnusedVariableCommand(document, diagnostic, lsContext));
            }
        }

        return actions;
    }

    public static CodeAction getRemoveUnusedVariableCommand(LSDocumentIdentifier document, Diagnostic diagnostic,
                                                            LSContext context) {
        Position position = diagnostic.getRange().getStart();
        int line = position.getLine();
        int column = position.getCharacter();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        CommandArgument lineArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, "" + line);
        CommandArgument colArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN, "" + column);
        CommandArgument uriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, uri);

        String diagnosedContent = getDiagnosedContent(diagnostic, context, document);
        Position afterAliasPos = offsetInvocation(diagnosedContent, position);
        BLangNode node = null;
        try {
            SymbolReferencesModel.Reference refAtCursor = getReferenceAtCursor(context, document, afterAliasPos);
            node = refAtCursor.getbLangNode();
        } catch (WorkspaceDocumentException | CompilationFailedException ignored) {

        }

        String codeActionMessage = "";
        if (node instanceof BLangFunction) {
            codeActionMessage = "Function";

        } else if (node instanceof BLangSimpleVariable || node instanceof BLangFieldBasedAccess) {
            if (node.type instanceof BUnionType) {
                codeActionMessage = "Object";

            } else {
                codeActionMessage = "Variable";
            }

        } else if (node instanceof BLangConstant) {
            codeActionMessage = "Constant";

        } else if (node instanceof BLangTypeDefinition) {
            codeActionMessage = "Type";

        } else if (node instanceof BLangService) {
            codeActionMessage = "Service";

        } else if (node instanceof BLangAnnotation) {
            codeActionMessage = "Annotation";

        } else if (node instanceof BLangXMLAttribute) {
            codeActionMessage = "XML";
        }

        codeActionMessage = "Remove unused " + codeActionMessage;

        List<Object> args = Arrays.asList(lineArg, colArg, uriArg);
        CodeAction action = new CodeAction(codeActionMessage);
        action.setCommand(new Command(CommandConstants.REMOVE_UNUSED_VARIABLE, UnusedVariableExecutor.COMMAND, args));
        return action;
    }
}

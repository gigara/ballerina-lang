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

package org.ballerinalang.langserver.command.executors;

import com.google.gson.JsonObject;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.unused.UnusedVisitor;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.langserver.command.CommandUtil.applyWorkspaceEdit;

/**
 * Command executor for remove unused variables.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class UnusedVariableExecutor implements LSCommandExecutor {

    public static final String COMMAND = "REMOVE_UNUSED_VARIABLE";

    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {

        String documentUri = null;
        int sLine = -1;
        int sCol = -1;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();

        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            String argKey = ((JsonObject) arg).get(ARG_KEY).getAsString();
            String argVal = ((JsonObject) arg).get(ARG_VALUE).getAsString();
            switch (argKey) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    documentUri = argVal;
                    textDocumentIdentifier.setUri(documentUri);
                    context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
                    break;
                case CommandConstants.ARG_KEY_NODE_LINE:
                    sLine = Integer.parseInt(argVal);
                    break;
                case CommandConstants.ARG_KEY_NODE_COLUMN:
                    sCol = Integer.parseInt(argVal);
                    break;
                default:
            }
        }

        if (sLine == -1 || sCol == -1 || documentUri == null) {
            throw new LSCommandExecutorException("Invalid parameters received for the change abstract type command!");
        }

        WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);

        Position position = new Position();
        position.setLine(sLine + 1);
        position.setCharacter(sCol + 1);
        List<BLangPackage> bLangPackages = null;
        try {
            bLangPackages = LSModuleCompiler.getBLangPackages(context, documentManager,
                                                              LSCustomErrorStrategy.class, true, false
                    , true);
        } catch (CompilationFailedException ignored) {

        }
        context.put(DocumentServiceKeys.BLANG_PACKAGES_CONTEXT_KEY, bLangPackages);

        UnusedVisitor unusedVisitor = new UnusedVisitor(position, context);

        BLangPackage currentBLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        currentBLangPackage.accept(unusedVisitor);

        DiagnosticPos nodePos = unusedVisitor.getDiagnosticPos();

        Position startPosition = new Position(nodePos.sLine - 1, nodePos.sCol - 1);
        Position endPosition = new Position(nodePos.eLine - 1, nodePos.eCol - 1);
        TextEdit textEdit = new TextEdit(new Range(startPosition, endPosition), "");

        LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_CLIENT_KEY);
        List<TextEdit> edits = new ArrayList<>();
        edits.add(textEdit);
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, edits);
        return applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), client);
    }

    @Override
    public String getCommand() {
        return COMMAND;
    }
}

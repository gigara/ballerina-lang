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

package org.ballerinalang.linter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.langserver.compiler.format.FormattingConstants;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.linter.Reference.ReferenceFinder;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.HashMap;
import java.util.List;

import static org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil.generateJSON;
import static org.ballerinalang.linter.LinteringNodeTree.lintErrors;

/**
 * Ballerina Linter compiler plugin.
 *
 * @since 1.0.1
 */
@SupportedAnnotationPackages(value = {"ballerina/openapi"})
public class LinterPlugin extends AbstractCompilerPlugin {
    private DiagnosticLog dLog = null;
    private static JsonObject model;

    @Override
    public void setCompilerContext(CompilerContext context) {

    }

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dLog = diagnosticLog;
    }

    @Override
    public void process(PackageNode packageNode) {

        WhitespaceVisitorEntry whitespaceVisitorEntry = new WhitespaceVisitorEntry();
        ReferenceFinder referenceFinder = new ReferenceFinder();

        // Collect endpoints throughout the package.
        for (CompilationUnitNode compilationUnitNode : packageNode.getCompilationUnits()) {

            JsonElement modelElement = null;
            try {
                modelElement = generateJSON(compilationUnitNode, new HashMap<>(), new HashMap<>());

            } catch (JSONGenerationException e) {
                e.printStackTrace();
            }
            model = modelElement.getAsJsonObject();

            whitespaceVisitorEntry.accept(model, compilationUnitNode);
            referenceFinder.visit((BLangCompilationUnit) compilationUnitNode);
            LineLengthAnalyzer.lintLineLength(model, compilationUnitNode, dLog);
        }

        // log diagnostics of whitespace linter
        List<JsonObject> ws = FormattingSourceGen.extractWS(model);
        int line = 1;
        int sCol = 1;
        int eCol = 1;

        for (JsonObject wsAr : ws) {
            if (lintErrors.size() == 0) {
                break;
            }
            int currentIndex = wsAr.get("i").getAsInt();
            boolean found = lintErrors.containsKey(wsAr.get("i").getAsInt());

            String wsStr = wsAr.get(FormattingConstants.WS).getAsString();
            String text = wsAr.get(FormattingConstants.TEXT).getAsString();

            if (wsStr.contains("\n")) {
                line++;
                String[] wsSplit = wsStr.split("\n");
                wsStr = wsSplit.length > 0 ? wsSplit[1] : "";

                for (int i = 1; i < wsSplit.length - 1; i++) {
                    line++;
                }

                if (!found && wsSplit.length > 0 && (wsSplit[wsSplit.length - 1].trim().length() == 0)) {
                    sCol = wsSplit[wsSplit.length - 1].length() + 1;
                    wsStr = "";
                } else {
                    sCol = 1;
                }
            }

            if (found) {
                eCol = sCol + wsStr.length();
                LintError error = lintErrors.get(currentIndex);
                Diagnostic.DiagnosticPosition pos = new DiagnosticPos(
                        (BDiagnosticSource) error.getCompilationUnitNode().getPosition().getSource(), line, line, sCol,
                        eCol);
                dLog.logDiagnostic(Diagnostic.Kind.WARNING, pos, error.getMessage());
                lintErrors.remove(currentIndex);
            } else {
                sCol = sCol + wsStr.length() + text.length();

            }
        }

        // log diagnostics of the reference finder
        referenceFinder.getDefinitions().forEach((integer, definition) -> {
                                                     if (definition.isHasDefinition() && !definition.isHasReference()) {
                                                         dLog.logDiagnostic(Diagnostic.Kind.WARNING, definition.getPosition(), definition.getSymbol().getName() + " is never used");
                                                     }
                                                 }
        );
    }

}
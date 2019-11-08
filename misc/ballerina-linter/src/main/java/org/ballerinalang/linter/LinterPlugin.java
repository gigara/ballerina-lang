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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import static org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil.generateJSON;
import static org.ballerinalang.linter.LinteringNodeTree.lintErrors;

/**
 * Ballerina Linter compiler plugin.
 *
 * @since 1.0.1
 */
@SupportedAnnotationPackages(value = {"ballerina/linter"})
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
            FormattingSourceGen.build(model, "CompilationUnit");

            lintErrors.clear();
            whitespaceVisitorEntry.accept(model, compilationUnitNode);
            referenceFinder.visit((BLangCompilationUnit) compilationUnitNode);
            LineLengthAnalyzer.lintLineLength(model, compilationUnitNode, dLog);
        }

        // log diagnostics of whitespace linter
        pushWhiteSpacesErrors(model, dLog);
        pushReferenceErrors(referenceFinder, dLog);
    }

    public void pushWhiteSpacesErrors(JsonObject model, DiagnosticLog dLog) {
        List<JsonObject> ws = FormattingSourceGen.extractWS(model);
        int sLine = 1;
        int eLine = 1;
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
            String[] wsSplit = new String[0];
            if (wsStr.contains("\n")) {
                wsSplit = new BufferedReader(new StringReader(wsStr))
                        .lines()
                        .toArray(String[]::new);

                String temp = wsStr.replace("\n", "");
                int noOfLines = (wsStr.length() - temp.length()) / "\n".length();

                // set eLine
                for (int i = 0; i < noOfLines; i++) {
                    eLine++;
                }

                // set column
                wsStr = wsSplit.length > 0 ? wsSplit[wsSplit.length - 1] : "";
                if (!found && wsSplit.length > 0) {
                    sCol = wsSplit[wsSplit.length - 1].length() + 1;
                    wsStr = "";
                } else {
                    sCol = 1;
                }

            }

            if (text.contains("\n")) {
                wsSplit = new BufferedReader(new StringReader(text))
                        .lines()
                        .toArray(String[]::new);

                String temp = text.replace("\n", "");
                int noOfLines = (text.length() - temp.length()) / "\n".length();

                // set eLine
                for (int i = 0; i < noOfLines; i++) {
                    eLine++;
                }
            }

            if (found) {
                // calculate starting line
                for (int i = wsSplit.length - 1; i >= 0; i--) {
                    if (wsSplit[i].trim().length() > 0) {
                        int linesBefore = (wsSplit.length - 1) - i;
                        sLine = eLine - (linesBefore > 0 ? linesBefore : 1);
                    }
                }

                /* calculate dlog ending column
                 * @source - https://stackoverflow.com/questions/22101186/how-to-get-leading-and-trailing-spaces-in-string-java
                 */
                int leadingWhitespaceLenth = wsStr.trim().length() == 0 ?
                        wsStr.length() : wsStr.replaceAll("^(\\s+).+", "$1").length() + 1;
                eCol = sCol + (leadingWhitespaceLenth == 0 ? text.length() : leadingWhitespaceLenth);
                LintError error = lintErrors.get(currentIndex);
                Diagnostic.DiagnosticPosition pos = new DiagnosticPos(
                        (BDiagnosticSource) error.getCompilationUnitNode().getPosition().getSource(),
                        sLine > 1 ? sLine : eLine, eLine, sCol,
                        eCol);
                dLog.logDiagnostic(Diagnostic.Kind.WARNING, pos, error.getMessage());
                lintErrors.remove(currentIndex);
                sCol = sCol + wsStr.length() + text.length();
                sLine = 1;
            } else {
                sCol = sCol + wsStr.length() + text.length();

            }
        }
    }

    public void pushReferenceErrors(ReferenceFinder referenceFinder, DiagnosticLog dLog) {
        // log diagnostics of the reference finder
        referenceFinder.getDefinitions().forEach((integer, definition) -> {
             if (definition.isHasDefinition() && !definition.isHasReference()) {
                 dLog.logDiagnostic(Diagnostic.Kind.WARNING,
                                    definition.getPosition(),
                                    definition.getSymbol().getName() + " is never used");
             }
         }
        );
    }
}

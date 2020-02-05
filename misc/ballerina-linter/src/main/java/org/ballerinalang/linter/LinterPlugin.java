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
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.linter.reference.ReferenceFinder;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.util.HashMap;

import static org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil.generateJSON;
import static org.ballerinalang.linter.LinterUtil.pushReferenceErrors;
import static org.ballerinalang.linter.LinterUtil.pushWhiteSpacesErrors;
import static org.ballerinalang.linter.LinteringNodeTree.lintErrors;

/**
 * Ballerina Linter compiler plugin.
 *
 * @since 1.2.0
 */
@SupportedAnnotationPackages(value = {"ballerina/linter"})
public class LinterPlugin extends AbstractCompilerPlugin {
    private DiagnosticLog dLog = null;
    private static JsonObject model;
    CompilerContext compilerContext;

    @Override
    public void setCompilerContext(CompilerContext context) {
        compilerContext = context;
    }

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dLog = diagnosticLog;
    }

    @Override
    public void process(PackageNode packageNode) {

        // check if skipped
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        boolean isSkipped = options.isSet(CompilerOptionName.LINTER_SKIPPED) && !Boolean.parseBoolean(
                options.get(CompilerOptionName.LINTER_SKIPPED));

        if (!isSkipped) {
            WhitespaceVisitorEntry whitespaceVisitorEntry = new WhitespaceVisitorEntry();
            ReferenceFinder referenceFinder = new ReferenceFinder();

            // Collect endpoints throughout the package.
            for (CompilationUnitNode compilationUnitNode : packageNode.getCompilationUnits()) {

                JsonElement modelElement = null;
                try {
                    modelElement = generateJSON(compilationUnitNode, new HashMap<>(), new HashMap<>());

                } catch (JSONGenerationException ignored) {

                }
                if (modelElement != null) {
                    setModel(modelElement.getAsJsonObject());
                }
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
    }

    /**
     * set the json model.
     * @param model compilation node as json
     */
    public static void setModel(JsonObject model) {
        LinterPlugin.model = model;
    }
}

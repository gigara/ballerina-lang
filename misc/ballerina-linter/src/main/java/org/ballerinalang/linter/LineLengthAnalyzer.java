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

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ballerina linter line length analyzer.
 *
 * @since 1.2.0
 */
class LineLengthAnalyzer {
    static void lintLineLength(JsonObject model, CompilationUnitNode compilationUnitNode, DiagnosticLog dLog) {
        String source = FormattingSourceGen.getSourceOf(model);
        AtomicInteger lineNo = new AtomicInteger(1);
        new BufferedReader(new StringReader(source))
                .lines().forEach(line -> {
            int length = line.trim().length();
            if (length > 120) {
                int sCol = line.length() - length + 1;
                Diagnostic.DiagnosticPosition pos = new DiagnosticPos(
                        (BDiagnosticSource) compilationUnitNode.getPosition().getSource(),
                        lineNo.get(), lineNo.get(), sCol, sCol + length);

                dLog.logDiagnostic(Diagnostic.Kind.WARNING, pos, "Line length should not exceed 120 chars");
            }
            lineNo.getAndIncrement();
        });
    }
}

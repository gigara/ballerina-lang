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

class LineLengthAnalyzer {
    static void lintLineLength(JsonObject model, CompilationUnitNode compilationUnitNode, DiagnosticLog dLog) {
        String source = FormattingSourceGen.getSourceOf(model);
        AtomicInteger lineNo = new AtomicInteger(1);
        new BufferedReader(new StringReader(source))
                .lines().forEach(line -> {
            int length = line.trim().length();
            if (length > 120) {
                int sCol = line.length() - length + 1;
                Diagnostic.DiagnosticPosition pos = new DiagnosticPos((BDiagnosticSource) compilationUnitNode.getPosition().getSource(),
                        lineNo.get(), lineNo.get(), sCol, sCol + length);

                dLog.logDiagnostic(Diagnostic.Kind.WARNING, pos, "Line length should not exceed 120 chars");
            }
            lineNo.getAndIncrement();
        });
    }
}

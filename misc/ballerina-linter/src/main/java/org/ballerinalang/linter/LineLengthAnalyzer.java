package org.ballerinalang.linter;

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

class LineLengthAnalyzer {
    static void lintLineLength(JsonObject model, CompilationUnitNode compilationUnitNode, DiagnosticLog dLog) {
        String source = FormattingSourceGen.getSourceOf(model);
        String[] lines = source.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int length = line.trim().length();
            if (length > 120) {
                int sCol = line.length() - length + 1;
                Diagnostic.DiagnosticPosition pos = new DiagnosticPos((BDiagnosticSource) compilationUnitNode.getPosition().getSource(),
                        i + 1, i + 1, sCol, sCol + length);

                dLog.logDiagnostic(Diagnostic.Kind.WARNING, pos, "Line length should not exceed 120 chars");
            }
        }
    }
}

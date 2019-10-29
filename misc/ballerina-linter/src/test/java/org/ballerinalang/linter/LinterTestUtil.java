package org.ballerinalang.linter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.linter.Reference.ReferenceFinder;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import static org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil.generateJSON;

public class LinterTestUtil {

    public static void compileAndLint(String ballerinaSource) {
        try {
            BallerinaFile ballerinaFile = ExtendedLSCompiler.compileContent(ballerinaSource, CompilerPhase.DEFINE);
            List<BLangCompilationUnit> compilationUnits = ballerinaFile.getBLangPackage().get().getCompilationUnits();
            BLangDiagnosticLog dLog = BLangDiagnosticLog.getInstance(ballerinaFile.getCompilerContext());

            WhitespaceVisitorEntry linteringVisitorEntry = new WhitespaceVisitorEntry();
            ReferenceFinder referenceFinder = new ReferenceFinder();
            // Collect endpoints throughout the package.
            for (CompilationUnitNode compilationUnitNode : compilationUnits) {

                JsonElement modelElement = null;
                try {
                    modelElement = generateJSON(compilationUnitNode, new HashMap<>(), new HashMap<>());

                } catch (JSONGenerationException e) {
                    e.printStackTrace();
                }
                JsonObject model = modelElement.getAsJsonObject();

                linteringVisitorEntry.accept(model, compilationUnitNode);
                referenceFinder.visit((BLangCompilationUnit) compilationUnitNode);

            }

            referenceFinder.getDefinitions().forEach((integer, definition) -> {
                        if (definition.isHasDefinition() && !definition.isHasReference()) {
                            dLog.logDiagnostic(Diagnostic.Kind.WARNING, definition.getPosition(), definition.getSymbol().getName() + " is never used");
                        }
                    }
            );
        } catch (CompilationFailedException e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(Path servicePath) throws IOException {
        return FileUtils.readFileToString(servicePath.toFile(), "UTF-8");
    }
}
package org.ballerinalang.linter;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.HashMap;
import static org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil.generateJSON;

@SupportedAnnotationPackages(value = {"ballerina/openapi"})
public class LinterPlugin extends AbstractCompilerPlugin {
    private DiagnosticLog dLog = null;

    @Override
    public void setCompilerContext(CompilerContext context) {

    }

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dLog = diagnosticLog;
    }

    @Override
    public void process(PackageNode packageNode) {
        // Collect endpoints throughout the package.
        for (CompilationUnitNode compilationUnitNode : packageNode.getCompilationUnits()) {

            JsonElement modelElement = null;
            try {
                modelElement = generateJSON(compilationUnitNode, new HashMap<>(), new HashMap<>());

            } catch (JSONGenerationException e) {
                e.printStackTrace();
            }
            JsonObject model = modelElement.getAsJsonObject();

            LinteringVisitorEntry linteringVisitorEntry = new LinteringVisitorEntry();
            linteringVisitorEntry.accept(model,compilationUnitNode,dLog);
        }
    }

}
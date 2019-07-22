package org.ballerinalang.linter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

import java.util.Map;

public class LinteringVisitorEntry {

    private LinteringVisitor visitor;

    public LinteringVisitorEntry() {
        this.visitor = new LinteringVisitor();

    }

    public void accept(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        visitor.beginVisit(node, compilationUnitNode, dLog);

        for (Map.Entry<String, JsonElement> child : node.entrySet()) {
            if (!child.getKey().equals("parent") && !child.getKey().equals("position") &&
                    !child.getKey().equals("ws")) {
                if (child.getValue().isJsonObject() && child.getValue().getAsJsonObject().has("kind")) {

                        child.getValue().getAsJsonObject().add("parent", node);
                        accept(child.getValue().getAsJsonObject(),compilationUnitNode, dLog);

                } else if (child.getValue().isJsonArray()) {
                    for (int i = 0; i < child.getValue().getAsJsonArray().size(); i++) {
                        JsonElement childItem = child.getValue().getAsJsonArray().get(i);
                        if (childItem.isJsonObject() && childItem.getAsJsonObject().has("kind")) {

                                childItem.getAsJsonObject().add("parent", node);
                                accept(childItem.getAsJsonObject(),compilationUnitNode, dLog);

                        }
                    }
                }
            }
        }

    }
}

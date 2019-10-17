package org.ballerinalang.linter;

import com.google.gson.JsonObject;
import org.ballerinalang.model.tree.Node;

public class LintError {
    private JsonObject currentWs;
    private String message;
    private Node compilationUnitNode;

    public LintError(JsonObject currentWs, String message, Node compilationUnitNode) {
        this.currentWs = currentWs;
        this.message = message;
        this.compilationUnitNode = compilationUnitNode;
    }

    public JsonObject getCurrentWs() {
        return currentWs;
    }

    public String getMessage() {
        return message;
    }

    public Node getCompilationUnitNode() {
        return compilationUnitNode;
    }
}

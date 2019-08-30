package org.ballerinalang.linter;

import com.google.gson.JsonObject;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LinteringVisitor {

    private boolean isFunctionMatch(JsonObject node, String methodName) {
        String functionToCall = String.format("lint%sNode", node.get("kind").getAsString());
        return functionToCall.equals(methodName);
    }

    public void beginVisit(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {

        LinteringNodeTree linteringNodeTree = new LinteringNodeTree();
        Class cls = linteringNodeTree.getClass();Method[] methods = cls.getMethods();for (Method method : methods) {
            if (isFunctionMatch(node, method.getName())) {Method methodcall1 = null;
                try {
                    methodcall1 = cls.getDeclaredMethod(method.getName(), node.getClass(), Node.class, DiagnosticLog.class);
                    methodcall1.invoke(cls.newInstance(), node, compilationUnitNode, dLog);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                        InstantiationException e) {
                    // TODO: Handle exception properly
                }
            }
        }
    }
}

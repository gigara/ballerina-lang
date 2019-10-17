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
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Ballerina linter white space visitor method invoker
 *
 * @since 1.0.1
 */
public class LinteringVisitor {

    private boolean isFunctionMatch(JsonObject node, String methodName) {
        String functionToCall = String.format("lint%sNode", node.get("kind").getAsString());
        return functionToCall.equals(methodName);
    }

    public void beginVisit(JsonObject node, Node compilationUnitNode) {

        LinteringNodeTree linteringNodeTree = new LinteringNodeTree();
        Class cls = linteringNodeTree.getClass();Method[] methods = cls.getMethods();for (Method method : methods) {
            if (isFunctionMatch(node, method.getName())) {Method methodcall1 = null;
                try {
                    methodcall1 = cls.getDeclaredMethod(method.getName(), node.getClass(), Node.class);
                    methodcall1.invoke(cls.newInstance(), node, compilationUnitNode);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                        InstantiationException e) {
                    // TODO: Handle exception properly
                }
            }
        }
    }
}

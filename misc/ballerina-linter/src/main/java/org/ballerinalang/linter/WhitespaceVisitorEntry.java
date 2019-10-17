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
import org.ballerinalang.model.tree.Node;

import java.util.Map;

/**
 * Ballerina linter whitespace visitor entry
 *
 * @since 1.0.1
 */
public class WhitespaceVisitorEntry {

    private LinteringVisitor visitor;

    public WhitespaceVisitorEntry() {
        this.visitor = new LinteringVisitor();

    }

    public void accept(JsonObject node, Node compilationUnitNode) {
        visitor.beginVisit(node, compilationUnitNode);

        for (Map.Entry<String, JsonElement> child : node.entrySet()) {
            if (!child.getKey().equals("parent") && !child.getKey().equals("position") &&
                    !child.getKey().equals("ws")) {
                if (child.getValue().isJsonObject() && child.getValue().getAsJsonObject().has("kind")) {

                    child.getValue().getAsJsonObject().add("parent", node);
                    accept(child.getValue().getAsJsonObject(), compilationUnitNode);

                } else if (child.getValue().isJsonArray()) {
                    for (int i = 0; i < child.getValue().getAsJsonArray().size(); i++) {
                        JsonElement childItem = child.getValue().getAsJsonArray().get(i);
                        if (childItem.isJsonObject() && childItem.getAsJsonObject().has("kind")) {

                            childItem.getAsJsonObject().add("parent", node);
                            accept(childItem.getAsJsonObject(), compilationUnitNode);

                        }
                    }
                }
            }
        }

    }
}

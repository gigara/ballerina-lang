/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.compiler.gigaFormat;

import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocuments;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test the parser.
 */
public class FormatUtil {

    private static final PrintStream OUT = System.out;

    public static void main(String[] args) throws IOException {
        String path =
                "/home/chamupathi/Documents/ballerina-lang/language-server/modules/langserver-compiler/src/test" +
                        "/resources/test1.bal";

        String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);

        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        FormattingTransformer formattingTransformer = new FormattingTransformer();
        formattingTransformer.accept(syntaxTree.modulePart());

        OUT.println("__________________________________________________");
        OUT.println("__________________________________________________");
        OUT.println();
        OUT.println(syntaxTree.toString());
        OUT.println();
        OUT.println("__________________________________________________");
        OUT.println("__________________________________________________");
    }

}

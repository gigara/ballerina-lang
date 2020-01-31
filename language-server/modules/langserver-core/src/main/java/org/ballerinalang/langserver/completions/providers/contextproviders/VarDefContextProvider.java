/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.completions.providers.contextproviders;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.sorters.ActionAndFieldAccessContextItemSorter;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser rule based variable definition statement context resolver.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class VarDefContextProvider extends AbstractCompletionProvider {
    public VarDefContextProvider() {
        this.attachmentPoints.add(BallerinaParser.VariableDefinitionStatementContext.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CompletionItem> getCompletions(LSContext context) {
        int invocationOrDelimiterTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);

        Class sorterKey;
        if (invocationOrDelimiterTokenType > -1) {
            sorterKey = ActionAndFieldAccessContextItemSorter.class;
        } else {
            sorterKey = context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY).getClass();
        }
        context.put(CompletionKeys.ITEM_SORTER_KEY, sorterKey);
        return new ArrayList<>(this.getVarDefExpressionCompletions(context, false));
    }
}

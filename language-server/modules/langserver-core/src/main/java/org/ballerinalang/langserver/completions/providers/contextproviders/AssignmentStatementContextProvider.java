/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.antlr.v4.runtime.CommonToken;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.builder.BFunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.sourceprune.SourcePruneKeys;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.common.utils.CommonUtil.getFunctionInvocationSignature;

/**
 * Context provider for Assignment statement.
 * 
 * @since 1.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class AssignmentStatementContextProvider extends AbstractCompletionProvider {

    public AssignmentStatementContextProvider() {
        this.attachmentPoints.add(BallerinaParser.AssignmentStatementContext.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext ctx) throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<Integer> defaultTokenTypes = ctx.get(SourcePruneKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        List<CommonToken> defaultTokens = ctx.get(SourcePruneKeys.LHS_DEFAULT_TOKENS_KEY);
        int assignTokenIndex = defaultTokenTypes.indexOf(BallerinaParser.ASSIGN);
        int newTokenIndex = defaultTokenTypes.indexOf(BallerinaParser.NEW);
        String lhsToken = defaultTokens.get(assignTokenIndex - 1).getText();
        Optional<BSymbol> lhsTokenSymbol = this.getSymbolByName(lhsToken, ctx).stream()
                .map(scopeEntry -> scopeEntry.symbol)
                .filter(symbol -> symbol instanceof BVarSymbol)
                .findFirst();
        
        if (lhsTokenSymbol.isPresent() && newTokenIndex >= 0) {
            return getCompletionsAfterNewKW(lhsTokenSymbol.get(), ctx);
        }

        Integer invocationTokenType = ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        if (invocationTokenType != -1) {
            /*
            Action invocation context
             */
            return this.getProvider(InvocationOrFieldAccessContextProvider.class).getCompletions(ctx);
        }

        if (lhsTokenSymbol.isPresent() && lhsTokenSymbol.get().type.tsymbol instanceof BObjectTypeSymbol) {
            BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) lhsTokenSymbol.get().type.tsymbol;
            BInvokableSymbol initFunction = objectTypeSymbol.initializerFunc.symbol;
            Pair<String, String> newSign = getFunctionInvocationSignature(initFunction, CommonKeys.NEW_KEYWORD_KEY,
                    ctx);
            CompletionItem cItem = BFunctionCompletionItemBuilder.build(initFunction, newSign.getRight(),
                    newSign.getLeft(), ctx);
            completionItems.add(new SymbolCompletionItem(ctx, initFunction, cItem));
        }

        List<Scope.ScopeEntry> filteredList = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        filteredList.removeIf(this.attachedSymbolFilter());
        filteredList.removeIf(scopeEntry -> scopeEntry.symbol instanceof BTypeSymbol);
        completionItems.addAll(this.getCompletionItemList(new ArrayList<>(filteredList), ctx));
        completionItems.addAll(this.getPackagesCompletionItems(ctx));
        fillStaticSnippetItems(completionItems, ctx);
        return completionItems;
    }

    private void fillStaticSnippetItems(List<LSCompletionItem> completionItems, LSContext context) {
        // Add the wait keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_WAIT.get()));
        // Add the start keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_START.get()));
        // Add the flush keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FLUSH.get()));
    }
    
    private List<LSCompletionItem> getCompletionsAfterNewKW(BSymbol lhsSymbol, LSContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (!(lhsSymbol.type.tsymbol instanceof BObjectTypeSymbol)) {
            return completionItems;
        }
        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) lhsSymbol.type.tsymbol;
        Integer invocationTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        if (invocationTokenType < 0) {
            completionItems.addAll(getPackagesCompletionItems(context));
        }
        BInvokableSymbol initFunction = objectTypeSymbol.initializerFunc.symbol;
        Pair<String, String> newSign = getFunctionInvocationSignature(initFunction, objectTypeSymbol.name.value,
                context);
        CompletionItem cItem = BFunctionCompletionItemBuilder.build(initFunction, newSign.getRight(),
                newSign.getLeft(), context);
        completionItems.add(new SymbolCompletionItem(context, initFunction, cItem));

        return completionItems;
    }
}

package org.ballerinalang.langserver.compiler.gigaFormat;

import io.ballerinalang.compiler.syntax.tree.*;

public class FormattingTransformer extends TreeModifier {
    private final MinutiaeList EMPTY_SPACE = MinutiaeList.emptyList();

    @Override
    public FunctionDefinitionNode transform(FunctionDefinitionNode functionDefinitionNode) {
        Token functionKeyword = getToken(functionDefinitionNode.functionKeyword());
        Token functionName = getToken(functionDefinitionNode.functionName());

        FunctionSignatureNode functionSignatureNode = functionDefinitionNode.functionSignature();
        Token functionSignatureOpenPara = getToken(functionSignatureNode.openParenToken());
        Token functionSignatureClosePara = getToken(functionSignatureNode.closeParenToken());

        return functionDefinitionNode.modify()
                .withFunctionKeyword(formatToken(functionKeyword, 0, 1))
                .withFunctionName((IdentifierToken) formatToken(functionName, 0, 0))
                .withFunctionSignature(functionSignatureNode.modify(functionSignatureOpenPara, functionSignatureNode.parameters(), functionSignatureClosePara, null))
                .withFunctionBody(functionDefinitionNode.functionBody())
              .apply();
    }

    @Override
    public FunctionBodyBlockNode transform(FunctionBodyBlockNode functionBodyBlockNode) {

        Token functionBodyOpenBrace = functionBodyBlockNode.openBraceToken();
        Token functionBodyCloseBrace = functionBodyBlockNode.closeBraceToken();

        return functionBodyBlockNode.modify()
                .withOpenBraceToken(formatToken(functionBodyOpenBrace, 1, 0))
                .apply();
    }

    private Token formatToken(Token token, int leadingSpaces, int trailingSpaces) {
        MinutiaeList leadingMinutiaeList = token.leadingMinutiae();
        MinutiaeList trailingMinutiaeList = token.trailingMinutiae();

        MinutiaeList newLeadingMinutiaeList = modifyMinutiaeList(leadingMinutiaeList, leadingSpaces);
        MinutiaeList newTrailingMinutiaeList = modifyMinutiaeList(trailingMinutiaeList, trailingSpaces);

        return token.modify(newLeadingMinutiaeList, newTrailingMinutiaeList);
    }

    private MinutiaeList modifyMinutiaeList(MinutiaeList minutiaeList, int spaces) {
        Minutiae minutiae = NodeFactory.createWhitespaceMinutiae(getWhiteSpaces(spaces));
        return minutiaeList.add(minutiae);
    }

    private String getWhiteSpaces(int column) {
        StringBuilder whiteSpaces = new StringBuilder();
        for (int i = 0; i <= (column - 1); i++) {
            whiteSpaces.append(" ");
        }

        return whiteSpaces.toString();
    }

    private <T extends Token> Token getToken(T node) {
        return node.modify(EMPTY_SPACE, EMPTY_SPACE);
    }
}

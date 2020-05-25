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

        FunctionBodyNode functionBodyNode = this.modifyNode(functionDefinitionNode.functionBody());

        return functionDefinitionNode.modify()
                .withFunctionKeyword(formatToken(functionKeyword, 0, 1))
                .withFunctionName((IdentifierToken) formatToken(functionName, 0, 0))
                .withFunctionSignature(functionSignatureNode.modify(functionSignatureOpenPara, functionSignatureNode.parameters(), functionSignatureClosePara, null))
                .withFunctionBody(functionBodyNode)
              .apply();
    }

    @Override
    public FunctionBodyBlockNode transform(FunctionBodyBlockNode functionBodyBlockNode) {

        Token functionBodyOpenBrace = getToken(functionBodyBlockNode.openBraceToken());
        Token functionBodyCloseBrace = getToken(functionBodyBlockNode.closeBraceToken());

        NodeList<StatementNode> statements = functionBodyBlockNode.statements();
        for (StatementNode statement : functionBodyBlockNode.statements()) {
            this.modifyNode(statement);
        }

        return functionBodyBlockNode.modify()
                .withOpenBraceToken(formatToken(functionBodyOpenBrace, 1, 0))
                .withCloseBraceToken(formatToken(functionBodyCloseBrace, 0, 0))
                .withStatements(statements)
                .apply();
    }

    @Override
    public ExpressionStatementNode transform(ExpressionStatementNode expressionStatementNode) {
        ExpressionNode expression = this.modifyNode(expressionStatementNode.expression());
        Token semicolonToken = expressionStatementNode.semicolonToken();

        return expressionStatementNode.modify()
                .withExpression(expression)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0))
                .apply();
    }

    @Override
    public FunctionCallExpressionNode transform(FunctionCallExpressionNode functionCallExpressionNode) {
        Node functionName = this.modifyNode(functionCallExpressionNode.functionName());

        return functionCallExpressionNode.modify()
                .withFunctionName(functionName)
                .apply();
    }

    @Override
    public QualifiedNameReferenceNode transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        Token modulePrefix = getToken(qualifiedNameReferenceNode.modulePrefix());
        Token identifier = getToken(qualifiedNameReferenceNode.identifier());

        return qualifiedNameReferenceNode.modify()
                .withModulePrefix(formatToken(modulePrefix, 0, 0))
                .withIdentifier((IdentifierToken) formatToken(identifier, 0, 0))
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

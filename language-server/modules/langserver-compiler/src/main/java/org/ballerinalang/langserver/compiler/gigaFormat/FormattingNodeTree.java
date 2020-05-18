//package org.ballerinalang.langserver.compiler.gigaFormat;
//
//import io.ballerinalang.compiler.internal.parser.tree.STFunctionBodyBlockNode;
//import io.ballerinalang.compiler.internal.parser.tree.STFunctionDefinitionNode;
//import io.ballerinalang.compiler.internal.parser.tree.STFunctionSignatureNode;
//import io.ballerinalang.compiler.internal.parser.tree.STNode;
//import io.ballerinalang.compiler.internal.parser.tree.STNodeList;
//import io.ballerinalang.compiler.internal.parser.tree.STToken;
//import io.ballerinalang.compiler.internal.parser.tree.SyntaxTrivia;
//import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
//
//public class FormattingNodeTree {
//    public void visit(STFunctionDefinitionNode functionDefinitionNode) {
//        // function keyword
//        STNodeList functionKeywordLeadingTrivia = getLeadingTrivia(functionDefinitionNode.functionKeyword);
//        setTrivia(0, functionKeywordLeadingTrivia);
//
//        STNodeList functionKeywordTrailingTrivia = getTrailingTrivia(functionDefinitionNode.functionKeyword);
//        setTrivia(1, functionKeywordTrailingTrivia);
//
//        // function name
//        STNodeList functionNameTrailingTrivia = getTrailingTrivia(functionDefinitionNode.functionName);
//        setTrivia(1, functionNameTrailingTrivia);
//
//        // function signature
//        STFunctionSignatureNode functionSignatureNode =
//                (STFunctionSignatureNode) functionDefinitionNode.functionSignature;
//
//        // opening param
//        STNodeList openingParenLeadingTrivia = getTrailingTrivia(functionSignatureNode.openParenToken);
//        setTrivia(0, openingParenLeadingTrivia);
//
//        // closing param
//        STNodeList closingParenTrailingTrivia = getTrailingTrivia(functionSignatureNode.closeParenToken);
//        setTrivia(1, closingParenTrailingTrivia);
//    }
//
//    public void visit(STFunctionBodyBlockNode functionBodyBlockNode) {
//        // closing brace
//        STNodeList closingBraceLeadingTrivia = getLeadingTrivia(functionBodyBlockNode.closeBraceToken);
//        setTrivia(0, closingBraceLeadingTrivia);
//    }
//
//    private STNodeList getLeadingTrivia(STNode token) {
//        return (STNodeList) ((STToken) token).leadingTrivia;
//    }
//
//    private STNodeList getTrailingTrivia(STNode token) {
//        return (STNodeList) ((STToken) token).trailingTrivia;
//    }
//
//    private void setTrivia(int noOfSpaces, STNodeList trivia) {
//        SyntaxTrivia syntaxTrivia = new SyntaxTrivia(SyntaxKind.WHITESPACE_TRIVIA, getWhiteSpaces(noOfSpaces));
////        trivia.childBuckets = new STNode[]{syntaxTrivia};
//        trivia.childBuckets[0] = syntaxTrivia;
//    }
//
//    private String getWhiteSpaces(int column) {
//        StringBuilder whiteSpaces = new StringBuilder();
//        for (int i = 0; i <= (column - 1); i++) {
//            whiteSpaces.append(" ");
//        }
//
//        return whiteSpaces.toString();
//    }
//}

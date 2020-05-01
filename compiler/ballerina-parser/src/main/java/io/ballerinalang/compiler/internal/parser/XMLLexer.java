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
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;

/**
 * A LL(k) lexer for XML in ballerina.
 * 
 * @since 2.0.0
 */
public class XMLLexer extends AbstractLexer {

    public XMLLexer(CharReader charReader) {
        super(charReader, ParserMode.XML_CONTENT);
    }

    /**
     * Get the next lexical token.
     * 
     * @return Next lexical token.
     */
    @Override
    public STToken nextToken() {
        switch (this.mode) {
            case XML_CONTENT:
                // XML content have no trivia. Whitespace is captured
                // as XML text.
                this.leadingTriviaList = new ArrayList<>(0);
                return readTokenInXMLContent();
            case XML_ELEMENT_START_TAG:
                processLeadingXMLTrivia();
                return readTokenInXMLElement(true);
            case XML_ELEMENT_END_TAG:
                processLeadingXMLTrivia();
                return readTokenInXMLElement(false);
            case XML_TEXT:
                // XML text have no trivia. Whitespace is part of the text.
                this.leadingTriviaList = new ArrayList<>(0);
                return readTokenInXMLText();
            case INTERPOLATION:
                this.leadingTriviaList = new ArrayList<>(0);
                return readTokenInInterpolation();
            default:
                // should never reach here.
                return null;
        }
    }

    /*
     * Private Methods
     */

    /**
     * Process whitespace up to an end of line.
     * <p>
     * <code>whitespace := 0x9 | 0xC | 0x20</code>
     * 
     * @return Whitespace trivia
     */
    private STNode processWhitespaces() {
        while (!reader.isEOF()) {
            char c = reader.peek();
            switch (c) {
                case LexerTerminals.SPACE:
                case LexerTerminals.TAB:
                case LexerTerminals.FORM_FEED:
                    reader.advance();
                    continue;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    break;
                default:
                    break;
            }
            break;
        }

        return STNodeFactory.createSyntaxTrivia(SyntaxKind.WHITESPACE_TRIVIA, getLexeme());
    }

    /**
     * Process end of line.
     * <p>
     * <code>end-of-line := 0xA | 0xD</code>
     * 
     * @return End of line trivia
     */
    private STNode processEndOfLine() {
        char c = reader.peek();
        switch (c) {
            case LexerTerminals.NEWLINE:
                reader.advance();
                return STNodeFactory.createSyntaxTrivia(SyntaxKind.END_OF_LINE_TRIVIA, getLexeme());
            case LexerTerminals.CARRIAGE_RETURN:
                reader.advance();
                if (reader.peek() == LexerTerminals.NEWLINE) {
                    reader.advance();
                }
                return STNodeFactory.createSyntaxTrivia(SyntaxKind.END_OF_LINE_TRIVIA, getLexeme());
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * <p>
     * Check whether a given char is a digit.
     * </p>
     * <code>Digit := 0..9</code>
     * 
     * @param c character to check
     * @return <code>true</code>, if the character represents a digit. <code>false</code> otherwise.
     */
    private boolean isDigit(int c) {
        return ('0' <= c && c <= '9');
    }

    /**
     * <p>
     * Check whether a given char is a hexa digit.
     * </p>
     * <code>HexDigit := Digit | a .. f | A .. F</code>
     * 
     * @param c character to check
     * @return <code>true</code>, if the character represents a hex digit. <code>false</code> otherwise.
     */
    private boolean isHexDigit(int c) {
        if ('a' <= c && c <= 'f') {
            return true;
        }
        if ('A' <= c && c <= 'F') {
            return true;
        }
        return isDigit(c);
    }

    /**
     * Returns the next character from the reader, without consuming the stream.
     * 
     * @return Next character
     */
    private int peek() {
        return this.reader.peek();
    }

    /**
     * Get the text associated with the current token.
     * 
     * @return Text associated with the current token.
     */
    private String getLexeme() {
        return reader.getMarkedChars();
    }

    private void reportLexerError(String message) {
        this.errorListener.reportInvalidNodeError(null, message);
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * XML_CONTENT Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    /**
     * Process leading trivia.
     */
    private void processLeadingXMLTrivia() {
        this.leadingTriviaList = new ArrayList<>(10);
        processXMLTrivia(this.leadingTriviaList, true);
    }

    /**
     * Process and return trailing trivia.
     * 
     * @return Trailing trivia
     */
    private STNode processTrailingXMLTrivia() {
        List<STNode> triviaList = new ArrayList<>(10);
        processXMLTrivia(triviaList, false);
        return STNodeFactory.createNodeList(triviaList);
    }

    /**
     * Process XML trivia and add it to the provided list.
     * <p>
     * <code>
     * xml-trivia := (WS | invalid-tokens)+ 
     * <br/><br/>
     * WS := #x20 | #x9 | #xD | #xA
     * </code>
     * 
     * @param triviaList List of trivia
     * @param isLeading Flag indicating whether the currently processing leading trivia or not
     */
    private void processXMLTrivia(List<STNode> triviaList, boolean isLeading) {
        while (!reader.isEOF()) {
            reader.mark();
            char c = reader.peek();
            switch (c) {
                case LexerTerminals.SPACE:
                case LexerTerminals.TAB:
                    triviaList.add(processWhitespaces());
                    break;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    triviaList.add(processEndOfLine());
                    if (isLeading) {
                        break;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private STToken getXMLSyntaxToken(SyntaxKind kind) {
        STNode leadingTrivia = STNodeFactory.createNodeList(this.leadingTriviaList);
        STNode trailingTrivia = processTrailingXMLTrivia();
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getXMLSyntaxToken(SyntaxKind kind, boolean allowLeadingWS, boolean allowTrailingWS) {
        STNode leadingTrivia = STNodeFactory.createNodeList(this.leadingTriviaList);
        if (!allowLeadingWS && leadingTrivia.bucketCount() != 0) {
            reportLexerError("invalid whitespace before: " + kind.stringValue());
        }

        STNode trailingTrivia = processTrailingXMLTrivia();
        if (!allowTrailingWS && trailingTrivia.bucketCount() != 0) {
            reportLexerError("invalid whitespace after: " + kind.stringValue());
        }
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getXMLSyntaxTokenWithoutTrailingWS(SyntaxKind kind) {
        STNode leadingTrivia = STNodeFactory.createNodeList(this.leadingTriviaList);
        STNode trailingTrivia = STNodeFactory.createNodeList(new ArrayList<>(0));
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getLiteral(SyntaxKind kind) {
        STNode leadingTrivia = STNodeFactory.createNodeList(this.leadingTriviaList);
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingXMLTrivia();
        return STNodeFactory.createLiteralValueToken(kind, lexeme, -1, leadingTrivia, trailingTrivia);
    }

    /**
     * Read the next token in the {@link ParserMode#XML_CONTENT} mode.
     * <p>
     * <code>
     * content :=  CharData? ((element | Reference | CDSect | PI | Comment) CharData?)*
     * </code>
     * 
     * @return Next token
     */
    private STToken readTokenInXMLContent() {
        reader.mark();
        if (reader.isEOF()) {
            return getXMLSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = reader.peek();
        switch (nextChar) {
            case LexerTerminals.BACKTICK:
                // Back tick means the end of currently processing XML content as
                // well as the end of entire XML literal. Hence end the context,
                // and start unwinding.
                endMode();
                return nextToken();
            case LexerTerminals.LT:
                reader.advance();
                nextChar = reader.peek();
                switch (nextChar) {
                    case LexerTerminals.EXCLAMATION_MARK:
                        // TODO: xml-comment
                        return null;
                    case LexerTerminals.QUESTION_MARK:
                        // TODO: XML-PI
                        return null;
                    case LexerTerminals.SLASH:
                        endMode();
                        startMode(ParserMode.XML_ELEMENT_END_TAG);
                        return getXMLSyntaxToken(SyntaxKind.LT_TOKEN, false, false);
                    default:
                        startMode(ParserMode.XML_ELEMENT_START_TAG);
                        return getXMLSyntaxToken(SyntaxKind.LT_TOKEN, false, false);
                }
            case LexerTerminals.DOLLAR:
                if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    // Switch to interpolation mode. Then the next token will be read in that mode.
                    startMode(ParserMode.INTERPOLATION);
                    reader.advance(2);
                    return getXMLSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                break;
            default:
                break;
        }

        // Everything else treat as charData
        startMode(ParserMode.XML_TEXT);
        return readTokenInXMLText();
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * XML_ELEMENT Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    /**
     * Read the next token in an XML element. Precisely this will operate on {@link ParserMode#XML_ELEMENT_START_TAG} or
     * {@link ParserMode#XML_ELEMENT_END_TAG} mode.
     * <p>
     * <code>element := EmptyElemTag | STag content ETag</code>
     * 
     * @param isStartTag Flag indicating whether the next token should be read in start-tag or the end-tag
     * @return Next token
     */
    private STToken readTokenInXMLElement(boolean isStartTag) {
        reader.mark();
        if (reader.isEOF()) {
            return getXMLSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int c = reader.peek();
        switch (c) {
            case LexerTerminals.LT:
                reader.advance();
                return getXMLSyntaxToken(SyntaxKind.LT_TOKEN, false, false);
            case LexerTerminals.GT:
                endMode();
                if (isStartTag) {
                    startMode(ParserMode.XML_CONTENT);
                }

                reader.advance();
                return getXMLSyntaxTokenWithoutTrailingWS(SyntaxKind.GT_TOKEN);
            case LexerTerminals.SLASH:
                reader.advance();
                return getXMLSyntaxToken(SyntaxKind.SLASH_TOKEN, false, false);
            case LexerTerminals.COLON:
                reader.advance();
                return getXMLSyntaxToken(SyntaxKind.COLON_TOKEN, false, false);
            case LexerTerminals.DOLLAR:
                // This is possible only in quoted-literals
                if (peek() == LexerTerminals.OPEN_BRACE) {
                    reader.advance();
                    startMode(ParserMode.INTERPOLATION);

                    // Trailing trivia should be captured similar to DEFAULT mode.
                    // Hence using the 'getSyntaxToken()' method.
                    return getXMLSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                reader.advance();
                break;
            case LexerTerminals.EQUAL:
                reader.advance();
                return getXMLSyntaxToken(SyntaxKind.EQUAL_TOKEN, true, true);
            case LexerTerminals.DOUBLE_QUOTE:
            case LexerTerminals.SINGLE_QUOTE:
                return processXMLQuotedString();
            case LexerTerminals.BACKTICK:
                // Back tick means the end of currently processing element as well as
                // the end of entire XML literal. Hence end the context, and start
                // unwinding.
                endMode();
                return nextToken();
            default:
                break;
        }

        return processXMLName(c);
    }

    /**
     * Process XML name token in the non-canonicalized form.
     * <p>
     * <code>
     * NCName := Name - (Char* ':' Char*)
     * <br/><br/>
     * Name := NameStartChar (NameChar)*
     * <br/><br/>
     * NameStartChar := ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D]
     *                | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF]
     *                | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
     * <br/><br/>
     * NameChar := NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
     * <br/><br/>
     * Char := #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     * 
     * </code>
     * 
     * 
     * @param startChar Starting character of the XML name
     * @return XML name token
     */
    private STToken processXMLName(int startChar) {
        boolean isValid = true;
        if (!XMLValidator.isNCNameStart(startChar)) {
            isValid = false;
        }

        while (!reader.isEOF() && XMLValidator.isNCName(peek())) {
            reader.advance();
        }

        String text = getLexeme();
        if (!isValid) {
            reportLexerError("invalid xml name: " + text);
        }

        return getXMLNameToken(text);
    }

    private STToken getXMLNameToken(String tokenText) {
        STNode leadingTrivia = STNodeFactory.createNodeList(this.leadingTriviaList);

        // TODO: some names can have whitespaces
        if (leadingTrivia.bucketCount() != 0) {
            reportLexerError("invalid whitespace before: " + tokenText);
        }

        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingXMLTrivia();
        return STNodeFactory.createIdentifierToken(lexeme, leadingTrivia, trailingTrivia);
    }

    private STToken processXMLQuotedString() {
        int startingQuote = peek();
        this.reader.advance();

        int nextChar;
        while (!reader.isEOF()) {
            nextChar = peek();
            switch (nextChar) {
                case LexerTerminals.DOUBLE_QUOTE:
                case LexerTerminals.SINGLE_QUOTE:
                    this.reader.advance();
                    if (nextChar == startingQuote) {
                        break;
                    }
                    continue;
                case LexerTerminals.BITWISE_AND:
                    processXMLReference(startingQuote);
                    continue;
                case LexerTerminals.LT:
                    reader.advance();
                    this.reportLexerError("'<' is not allowed in XML attribute value");
                    continue;
                default:
                    this.reader.advance();
                    continue;
            }
            break;
        }

        if (reader.isEOF()) {
            reportLexerError("missing " + String.valueOf((char) startingQuote));
        }

        return getLiteral(SyntaxKind.STRING_LITERAL);
    }

    /**
     * Process XML references.
     * <p>
     * <code>
     * Reference := EntityRef | CharRef
     * </code>
     * 
     * @param startingQuote Starting quote
     */
    private void processXMLReference(int startingQuote) {
        this.reader.advance();
        int nextChar = peek();
        switch (nextChar) {
            case LexerTerminals.SEMICOLON:
                reportLexerError("missing entity reference name");
                reader.advance();
                return;
            case LexerTerminals.HASH:
                processXMLCharRef(startingQuote);
                break;
            case LexerTerminals.DOUBLE_QUOTE:
            case LexerTerminals.SINGLE_QUOTE:
                if (nextChar == startingQuote) {
                    break;
                }
                // fall through
            default:
                // Process the name component
                processXMLEntityRef(startingQuote);
                break;
        }

        // Process ending semicolon
        if (peek() == LexerTerminals.SEMICOLON) {
            reader.advance();
        } else {
            reportLexerError("missing ; in reference");
        }
    }

    /**
     * Process XML char references.
     * <p>
     * <code>
     * CharRef := ('&#' [0-9]+ ';') | ('&#x' [0-9a-fA-F]+ ';')
     * </code>
     * 
     * @param startingQuote Starting quote
     */
    private void processXMLCharRef(int startingQuote) {
        // We reach here after consuming '&', and verifying the following '#'.
        // Hence consume the hash token.
        reader.advance();
        int nextChar = peek();
        if (nextChar == 'x') {
            processHexDigits();
        } else {
            processDigits();
        }
    }

    private void processHexDigits() {
        int nextChar;
        while (!reader.isEOF()) {
            nextChar = peek();
            switch (nextChar) {
                case LexerTerminals.DOUBLE_QUOTE:
                case LexerTerminals.SINGLE_QUOTE:
                case LexerTerminals.SEMICOLON:
                    return;
                default:
                    if (!isHexDigit(nextChar)) {
                        reportLexerError("invalid hex digit in char ref");
                    }
                    reader.advance();
                    break;
            }
        }
    }

    private void processDigits() {
        int nextChar;
        while (!reader.isEOF()) {
            nextChar = peek();
            switch (nextChar) {
                case LexerTerminals.DOUBLE_QUOTE:
                case LexerTerminals.SINGLE_QUOTE:
                case LexerTerminals.SEMICOLON:
                    return;
                default:
                    if (!isDigit(nextChar)) {
                        reportLexerError("invalid digit in char ref");
                    }
                    reader.advance();
                    break;
            }
        }
    }

    /**
     * Process the name component of an XML entity reference. This method will
     * advance the token reader until the end of entity reference.
     * 
     * <p>
     * <code>EntityRef := '&' Name ';'</code>
     * 
     * @param startingQuote Starting quote
     */
    private void processXMLEntityRef(int startingQuote) {
        // We reach here after consuming '&'

        // Process the name component
        if (!XMLValidator.isNCNameStart(peek())) {
            reportLexerError("invalid entity reference name start");
        } else {
            reader.advance();
        }

        while (!reader.isEOF() && XMLValidator.isNCName(peek())) {
            reader.advance();
        }
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * XML_TEXT Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    /**
     * Read token form the XML text (i.e.: charData).
     * <p>
     * <code>
     * text := CharData?
     * <br/>
     * CharData :=  [^<&]* - ([^<&]* ']]>' [^<&]*)
     * </code>
     * 
     * @return Next token
     */
    private STToken readTokenInXMLText() {
        reader.mark();
        if (reader.isEOF()) {
            return getXMLSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar;
        while (!reader.isEOF()) {
            nextChar = this.reader.peek();
            switch (nextChar) {
                case LexerTerminals.LT:
                    break;
                case LexerTerminals.DOLLAR:
                    if (this.reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                        break;
                    }
                    reader.advance();
                    continue;
                case LexerTerminals.BITWISE_AND:
                    // The ampersand character is not allowed in text. Report error, but continue.
                    reportLexerError("invalid token in xml text: " + String.valueOf((char) nextChar));
                    reader.advance();
                    continue;
                case LexerTerminals.BACKTICK:
                    break;
                default:
                    // TODO: ']]>' should also terminate charData?
                    reader.advance();
                    continue;
            }

            break;
        }

        // End of charData also means the end of XML_TEXT mode
        endMode();
        return getXMLText(SyntaxKind.XML_TEXT);
    }

    private STToken getXMLText(SyntaxKind kind) {
        STNode leadingTrivia = STNodeFactory.createNodeList(this.leadingTriviaList);
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingXMLTrivia();
        return STNodeFactory.createLiteralValueToken(kind, lexeme, -1, leadingTrivia, trailingTrivia);
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * INTERPOLATION Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readTokenInInterpolation() {
        int nextToken = peek();
        switch (nextToken) {
            case LexerTerminals.CLOSE_BRACE:
                // Close-brace in the interpolation mode definitely means its
                // then end of the interpolation.
                endMode();
                reader.advance();
                return getXMLSyntaxTokenWithoutTrailingWS(SyntaxKind.CLOSE_BRACE_TOKEN);
            default:
                // We should never reach here. Interpolation must be empty since
                // this is something we are injecting. This is just a fail-safe.
                endMode();
                return nextToken();
        }
    }
}

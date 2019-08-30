package org.ballerinalang.linter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.compiler.format.FormattingConstants;
import org.ballerinalang.langserver.compiler.format.Tokens;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

public class LinteringNodeTree {

    /**
     * Log error to the ide
     *
     * @param  node {JsonObject} node as json object
     * @param  compilationUnitNode {Node} compilation unit node as a Node object
     * @param  messege {String} error messege as a string
     */
    private void logError(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog, String messege){
        // get error position
        int sLine = node.get("position").getAsJsonObject().get("startLine").getAsInt();
        int eLine = node.get("position").getAsJsonObject().get("endLine").getAsInt();
        int sCol = node.get("position").getAsJsonObject().get("startColumn").getAsInt();
        int eCol = node.get("position").getAsJsonObject().get("endColumn").getAsInt();

        // set position
        Diagnostic.DiagnosticPosition pos = new DiagnosticPos((BDiagnosticSource) compilationUnitNode.getPosition().getSource(),sLine,eLine,sCol,eCol);
        pos.setStartLine(sLine);
        pos.setEndLine(eLine);
        pos.setStartColumn(sCol);
        pos.setEndColumn(eCol);

         dLog.logDiagnostic(Diagnostic.Kind.WARNING, pos, messege);
    }

    private void logError(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog, String messege, int sL, int eL, int sC, int eC){
        // get error position
        int sLine;
        int eLine;
        int sCol;
        int eCol;

        if(sL < 0) {
            sLine = node.get("position").getAsJsonObject().get("startLine").getAsInt();
        }else{
            sLine = sL;
        }
        if(eL < 0) {
            eLine = node.get("position").getAsJsonObject().get("endLine").getAsInt();
        }else{
            eLine = eL;
        }
        if(sC < 0) {
            sCol = node.get("position").getAsJsonObject().get("startColumn").getAsInt();
        }else{
            sCol = sC;
        }
        if(eC < 0) {
            eCol = node.get("position").getAsJsonObject().get("endColumn").getAsInt();
        }else{
            eCol = eC;
        }

        // set position
        Diagnostic.DiagnosticPosition pos = new DiagnosticPos((BDiagnosticSource) compilationUnitNode.getPosition().getSource(),sLine,eLine,sCol,eCol);
        pos.setStartLine(sLine);
        pos.setEndLine(eLine);
        pos.setStartColumn(sCol);
        pos.setEndColumn(eCol);

        dLog.logDiagnostic(Diagnostic.Kind.WARNING, pos, messege);
    }
    /**
     * lint Import node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintImportNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {

            JsonArray ws = node.get(FormattingConstants.WS).getAsJsonArray();

            // whitespace for semicolon
            JsonObject semicolonWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (getWhiteSpaceCount(semicolonWhitespace.get(FormattingConstants.WS).getAsString()) >0) {
                logError(node,compilationUnitNode,dLog,"Irregular whitespace near the ';'");
            }

            // whitespace for dash in pkg
            for(int i=0; i<ws.size(); i++){
                if(ws.get(i).getAsJsonObject().get("text").getAsString().equals("/")){
                    if(getWhiteSpaceCount(ws.get(i).getAsJsonObject().get(FormattingConstants.WS).getAsString()) > 0 ||
                            getWhiteSpaceCount(ws.get(i+1).getAsJsonObject().get(FormattingConstants.WS).getAsString()) > 0){
                        logError(node,compilationUnitNode,dLog,"Irregular whitespace near the '/'");
                    }
                }
            }

        }
    }

    /**
     * lint If node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintIfNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Update if or else if keyword.
            JsonObject firstKeywordWS = ws.get(0).getAsJsonObject();
            if (node.has("isElseIf  Block") && node.get("isElseIfBlock").getAsBoolean()) {

                // Update if keyword whitespace.
                JsonObject ifKeywordWS = ws.get(1).getAsJsonObject();
                if (this.noHeightAvailable(ifKeywordWS.get(FormattingConstants.WS).getAsString())) {
                    logError(node, compilationUnitNode, dLog, "Irregular whitespace near the 'if'");
                }
            } else {
                if (this.noHeightAvailable(firstKeywordWS.get(FormattingConstants.WS).getAsString())) {
                    logError(node, compilationUnitNode, dLog, "Irregular whitespace near the 'if'");
                }
            }

            // Update opening brace whitespace.
            JsonObject openingBraceWS = ws.get(ws.size() - 2).getAsJsonObject();
            if (this.noHeightAvailable(openingBraceWS.get(FormattingConstants.WS).getAsString())) {
                if (!openingBraceWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)) {
                    logError(node, compilationUnitNode, dLog, "Irregular whitespace near the '{'");
                }
            }

            // Update closing brace whitespace
            JsonObject closingBraceWS = ws.get(ws.size() - 1).getAsJsonObject();
            if (closingBraceWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                logError(node, compilationUnitNode, dLog, "Irregular whitespace near the '}'");
            }

            if (node.has("elseStatement")) {

                JsonObject elseStatement = node.get("elseStatement").getAsJsonObject();

                // before the begin space
                if(!elseStatement.get(FormattingConstants.WS).getAsJsonArray().get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                    logError(elseStatement, compilationUnitNode, dLog, "Irregular whitespace near the 'else'");
                }

                // opening bracket space
                if(!elseStatement.get(FormattingConstants.WS).getAsJsonArray().get(1).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                    logError(elseStatement, compilationUnitNode, dLog, "Irregular whitespace near the '{'");
                }

                // closing bracket space
                if(elseStatement.get(FormattingConstants.WS).getAsJsonArray().get(2).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                    logError(elseStatement, compilationUnitNode, dLog, "Irregular whitespace near the '}'");
                }
            }

            // condition spaces check
            if (node.has("condition")) {
                JsonObject conditionWs = node.getAsJsonObject("condition");
                // opening bracket
                if (!conditionWs.get(FormattingConstants.WS).getAsJsonArray().get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                    logError(conditionWs, compilationUnitNode, dLog, "Irregular whitespace near the '('");

                }
                // closing bracket
                if(!conditionWs.get(FormattingConstants.WS).getAsJsonArray().get(1).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                    logError(conditionWs, compilationUnitNode, dLog, "Irregular whitespace near the ')'");
                }
                // condition begin
                if(!conditionWs.get("expression").getAsJsonObject().get("expression").getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray().get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                    logError(conditionWs.get("expression").getAsJsonObject(), compilationUnitNode, dLog, "Irregular whitespace near the '('");
                }
            }
        }
    }

    /**
     * lint Service node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintServiceNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            if (node.has("anonymousService") && node.get("anonymousService").getAsBoolean()) {
                // Update whitespaces for service definition.
                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        if (text.equals(Tokens.SERVICE)) {


                        } else {
                            // Update service identifier whitespaces and on keyword.

                        }
                    }
                }

                // Update whitespaces for resources.
                if (node.has("resources")) {
                    JsonArray resources = node.getAsJsonArray("resources");
                    //iterateAndFormatBlockStatements(indentationOfParent, indentationOfParent, resources);
                }

                // Update whitespaces of the annotation attachments.
                if (node.has("annotationAttachments")) {
                    JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                    for (int i = 0; i < annotationAttachments.size(); i++) {
                        JsonObject annotationAttachment = annotationAttachments.get(i).getAsJsonObject();
                        JsonObject annotationAttachmentFormattingConfig;
                        //
                    }
                }
            } else {

                // whitespaces for service definition.
                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        // service keyword
                        if (text.equals(Tokens.SERVICE) && !currentWS.get(FormattingConstants.WS).getAsString().equals("\n")) {
                            logError(node, compilationUnitNode, dLog, "Irregular whitespace near the 'service' keyword");

                        } else if (text.equals(Tokens.COMMA)) {
                            // TODO

                        } else if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                            // service identifier whitespaces and on keyword.
                            logError(node.get("name").getAsJsonObject(), compilationUnitNode, dLog, "Irregular whitespace near keyword");
                        }
                    }
                }

            }
        }
    }

    /**
     * lint Function node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintFunctionNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {

        // Functions

            JsonArray parameters = node.get("parameters").getAsJsonArray();

            JsonArray functionWS = node.get(FormattingConstants.WS).getAsJsonArray();
            for (int i=1; i<functionWS.size()-1; i++){
                if(functionWS.get(i).getAsJsonObject().get(FormattingConstants.TEXT).getAsString().equals(Tokens.OPENING_PARENTHESES)
                        || functionWS.get(i).getAsJsonObject().get(FormattingConstants.TEXT).getAsString().equals(Tokens.CLOSING_PARENTHESES)
                        || functionWS.get(i).getAsJsonObject().get(FormattingConstants.TEXT).getAsString().equals(Tokens.COMMA)){

                    if(!functionWS.get(i).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                        logError(node.get("name").getAsJsonObject(), compilationUnitNode, dLog, "Irregular whitespace");
                    }
                }
                else if(!functionWS.get(i).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                    logError(node, compilationUnitNode, dLog, "Irregular whitespace");
                }
            }

            // function parameter
            for (int i = 0; i < parameters.size(); i++) {
                JsonArray parameterAttr = parameters.get(i).getAsJsonObject().get(FormattingConstants.TYPE_NODE).getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray();
                JsonObject position = parameters.get(i).getAsJsonObject();
                for (int j = 0; j < parameterAttr.size(); j++) {
                    if (i > 0 && j == 0) {
                        if (!parameterAttr.get(j).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)) {
                            logError(position, compilationUnitNode, dLog, "Irregular whitespace");
                        }
                    } else if (!parameterAttr.get(j).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                        logError(position, compilationUnitNode, dLog, "Irregular whitespace");
                    }
                }

                // function parameter name
                if (!parameters.get(i).getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray().get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)) {
                    logError(position, compilationUnitNode, dLog, "Irregular whitespace");
                }
            }
    }

    /**
     * lint Invocation node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintInvocationNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            boolean isExpressionAvailable = node.has(FormattingConstants.EXPRESSION)
                    && node.getAsJsonObject(FormattingConstants.EXPRESSION).has(FormattingConstants.WS);;
            boolean isAsync = false;
            boolean isCheck = false;
            boolean isActionOrFieldInvocation = false;
            JsonObject identifierWhitespace = null;
            String expressionName = null;

            if (isExpressionAvailable) {
                expressionName = node.getAsJsonObject(FormattingConstants.EXPRESSION).has("variableName") ?
                        node.getAsJsonObject(FormattingConstants.EXPRESSION).get("variableName")
                                .getAsJsonObject().get("value").getAsString() : null;

            }

            for (int i = 0; i < ws.size(); i++) {
                JsonObject invocationWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(invocationWS.get(FormattingConstants.WS).getAsString())) {
                     String text = invocationWS.get(FormattingConstants.TEXT).getAsString();

                    if (text.equals(Tokens.DOT)) {
                        // Handle before whitespaces.
                        if(!invocationWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }
                        // after whitespace.
                        identifierWhitespace = ws.get(i + 1).getAsJsonObject();
                        if (this.noHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())
                                && !identifierWhitespace.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }

                    }else if (text.equals(Tokens.RIGHT_ARROW)){
                        // Handle colon whitespaces.
                        if(!invocationWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }
                        // Identifier whitespace.
                        identifierWhitespace = ws.get(i + 1).getAsJsonObject();
                        if (this.noHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())
                                && !identifierWhitespace.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }

                     }else if (text.equals(Tokens.OPENING_PARENTHESES) && !invocationWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                        logError(node, compilationUnitNode, dLog, "Irregular White Space");

                    } else if (text.equals(Tokens.COMMA) && !invocationWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                        logError(node, compilationUnitNode, dLog, "Irregular White Space");

                    } else if (text.equals(Tokens.CLOSING_PARENTHESES) && !invocationWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                        logError(node, compilationUnitNode, dLog, "Irregular White Space");

                    } else if (text.equals(Tokens.COLON)) {
                        // Handle colon whitespaces.
                        if(!invocationWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }
                        // Identifier whitespace.
                        identifierWhitespace = ws.get(i + 1).getAsJsonObject();
                        if (this.noHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())
                                && !identifierWhitespace.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }
                    }
                }
            }

            // Update argument expressions whitespaces.
            if (node.has("argumentExpressions")) {
                JsonArray argumentExpressions = node.getAsJsonArray("argumentExpressions");
                if (expressionName != null) {
                    for(int i=0; i<argumentExpressions.size(); i++) {
                        JsonElement argument = argumentExpressions.get(i);
                        JsonArray argumentWS = argument.getAsJsonObject().has(FormattingConstants.WS) ?
                                argument.getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray() : null;
                        if (argumentWS != null) {
                                JsonElement wsItem = argumentWS.get(0);
                                JsonObject currentWS = wsItem.getAsJsonObject();
                                if (!currentWS.get(FormattingConstants.TEXT).getAsString().equals(expressionName)) {
                                    if (i == 0 && !argument.getAsJsonObject().has("operatorKind")) {
                                        if (!(currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE))) {
                                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                                        }
                                    } else if (!(currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE))) {
                                        logError(node, compilationUnitNode, dLog, "Irregular White Space");
                                    }
                                }
                        }
                    }
                }

            }

            if (node.has("requiredArgs")) {
                JsonArray requiredArgs = node.getAsJsonArray("requiredArgs");
                if (expressionName != null) {
                    for(int i=0; i<requiredArgs.size(); i++) {
                        JsonElement argument = requiredArgs.get(i);
                        JsonArray ArgWS = argument.getAsJsonObject().has(FormattingConstants.WS) ?
                                argument.getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray() : null;
                        if (ArgWS != null) {
                            for (JsonElement wsItem : ArgWS) {
                                JsonObject currentWS = wsItem.getAsJsonObject();
                                if (!currentWS.get(FormattingConstants.TEXT).getAsString().equals(expressionName)) {
                                    if (i == 0  && !argument.getAsJsonObject().has("operatorKind")) {
                                        if (!(currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE))) {
                                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                                        }
                                    } else if (!(currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE))) {
                                        logError(node, compilationUnitNode, dLog, "Irregular White Space");
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * lint Variable Def node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintVariableDefNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
            boolean isVarExists = false;

            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
                // Iterate and update whitespaces for variable def.
                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    String cws = currentWS.get(FormattingConstants.WS).getAsString();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        if (text.equals("var") && !cws.equals(FormattingConstants.SINGLE_SPACE)) {
                            //logError(node, compilationUnitNode, dLog, "Irregular White Space");
                            //TODO
                        }

                        if (text.equals(Tokens.EQUAL) && !cws.equals(FormattingConstants.SINGLE_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }

                        if (text.equals(Tokens.SEMICOLON) && !cws.equals(FormattingConstants.EMPTY_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }
                    }
                }
            }
    }

    /**
     * lint Variable node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintVariableNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {

        if(node.has(FormattingConstants.WS)){
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            boolean isColonAvailable = false;
            for (int i = 0; i < ws.size(); i++) {
                JsonObject currentWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    String WStext = currentWS.get(FormattingConstants.WS).getAsString();
                    if (text.equals(Tokens.FINAL)
                            || text.equals(Tokens.PUBLIC)
                            || text.equals(Tokens.PRIVATE)
                            || text.equals(Tokens.CONST)
                            || text.equals(Tokens.VAR)
                            || text.equals(Tokens.CLIENT)
                            || text.equals(Tokens.LISTENER)
                            || text.equals(Tokens.ABSTRACT)
                            || text.equals(Tokens.CHANNEL)
                            || text.equals(Tokens.OBJECT)) {

                        //TODO
                    } else if (text.equals(Tokens.SEMICOLON) || text.equals(Tokens.QUESTION_MARK)
                            || text.equals(Tokens.COMMA)) {

                        if (!WStext.equals(FormattingConstants.EMPTY_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+text);
                        }

                    } else if (text.equals(Tokens.OPENING_BRACE)) {
                        if (!WStext.equals(FormattingConstants.SINGLE_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+text);
                        }
                    } else if (text.equals(Tokens.CLOSING_BRACE)) {
                        //TODO
                    } else if (text.equals(Tokens.ELLIPSIS)) {
                        if (!WStext.equals(FormattingConstants.EMPTY_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+text);
                        }
                    } else if (text.equals(Tokens.EQUAL)) {
                        if (!WStext.equals(FormattingConstants.SINGLE_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+text);
                        }
                    } else if (text.equals(Tokens.COLON)) {
                        if (!WStext.equals(FormattingConstants.EMPTY_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+text);
                        }
                        isColonAvailable = true;
                    }else if (!WStext.equals(FormattingConstants.SINGLE_SPACE)) {
                        logError(node, compilationUnitNode, dLog, "Irregular White Space near "+text);
                    }
                    //TODO
                }
            }
        }
    }

    /**
     * lint Value Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintValueTypeNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            JsonObject typeWhitespace = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(typeWhitespace.get(FormattingConstants.WS).getAsString())) {
                if(node.get(FormattingConstants.PARENT).getAsJsonObject().get(FormattingConstants.KIND).getAsString().equals("Variable")
                  && !node.get(FormattingConstants.PARENT).getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray().get(0)
                        .getAsJsonObject().get(FormattingConstants.TEXT).getAsString().equals("foreach"))
                    logError(node, compilationUnitNode, dLog, "Variable should declared in a new line.");
            }
        }
    }

    /**
     * lint Union Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintUnionTypeNodeNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {

            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

                // Iterate through WS to update horizontal whitespaces.
                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                        String wsText = currentWS.get(FormattingConstants.WS).getAsString();
                        // Update opening parentheses whitespace.
                        if (text.equals(Tokens.OPENING_PARENTHESES) && !wsText.equals(FormattingConstants.EMPTY_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Variable should declared in a new line.");

                        } else if (text.equals("|") && !wsText.equals(FormattingConstants.EMPTY_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Variable should declared in a new line.");

                        } else if (text.equals(Tokens.QUESTION_MARK) && !wsText.equals(FormattingConstants.EMPTY_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Variable should declared in a new line.");

                        } else if (currentWS.get(FormattingConstants.TEXT).getAsString()
                                .equals(Tokens.CLOSING_PARENTHESES) && !wsText.equals(FormattingConstants.EMPTY_SPACE)) {
                            logError(node, compilationUnitNode, dLog, "Variable should declared in a new line.");

                        }
                    }
                }
            }

            //TODO
            // Update member types whitespaces.
            if (node.has("memberTypeNodes")) {
                JsonArray memberTypeNodes = node.getAsJsonArray("memberTypeNodes");
                for (int i = 0; i < memberTypeNodes.size(); i++) {
                    JsonObject memberType = memberTypeNodes.get(i).getAsJsonObject();
                    JsonObject memberTypeFormatConfig;
                    if (i == 0 && !node.getAsJsonObject(FormattingConstants.PARENT)
                            .get(FormattingConstants.KIND).getAsString().equals("TypeDefinition")) {

                    } else if(i!=0){

                    }
                }
            }
    }

    /**
     * lint Type Test Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintTypeTestExprNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Iterate through whitespaces for this node and update.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.IS)) {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE))
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near 'is' keyword");
                    }
                }
            }
        }
    }

    /**
     * lint Type Init Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintTypeInitExprNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Iterate and update whitespaces for node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.NEW)) {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE))
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near 'new' keyword");

                    } else if (text.equals(Tokens.OPENING_PARENTHESES)
                                ||text.equals(Tokens.COMMA) || text.equals(Tokens.CLOSING_PARENTHESES)) {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE))
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near 'is' keyword");
                    }
                }
            }

            // Iterate and format expressions.
            if (node.has(FormattingConstants.EXPRESSIONS)) {
                JsonArray expressions = node.getAsJsonArray(FormattingConstants.EXPRESSIONS);
                if(expressions.size() > 0){
                    for(JsonElement expression : expressions){
                        JsonObject currentExpression = expression.getAsJsonObject();
                        JsonArray expressionWS = currentExpression.get(FormattingConstants.WS).getAsJsonArray();
                        for(JsonElement wsItem : expressionWS){
                            if(!wsItem.getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE))
                                logError(currentExpression, compilationUnitNode, dLog, "Irregular White Space");
                        }
                    }
                }
            }

            // Handle type name formatting.
            if (node.has(FormattingConstants.TYPE)) {
                JsonObject type = node.getAsJsonObject(FormattingConstants.TYPE);
                if(!type.isJsonNull()){
                        JsonArray typeWS = type.get(FormattingConstants.WS).getAsJsonArray();
                        for(int i=0; i<typeWS.size(); i++){
                            if(i == 0){
                                if(!typeWS.get(i).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE))
                                    logError(type, compilationUnitNode, dLog, "Irregular White Space");
                            }
                            else if(!typeWS.get(i).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                                logError(type, compilationUnitNode, dLog, "Irregular White Space");
                            }
                    }
                }
            }
        }
    }

    /**
     * lint Type Definition node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintTypeDefinitionNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.TYPE_NODE)) {
            JsonObject typeNode = node.getAsJsonObject(FormattingConstants.TYPE_NODE);
            JsonArray ws = typeNode.get(FormattingConstants.WS).getAsJsonArray();
            JsonObject pos = typeNode.getAsJsonObject(FormattingConstants.POSITION);
            for(JsonElement currentWS : ws){
                if(currentWS.getAsJsonObject().get(FormattingConstants.TEXT).getAsString().equals(Tokens.OPENING_BRACE)){
                    int wsLength = getWhiteSpaceCount(currentWS.getAsJsonObject().get(FormattingConstants.WS).getAsString());
                    int sPos = pos.get("startColumn").getAsInt();
                    if(!currentWS.getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE))
                        logError(node, compilationUnitNode, dLog, "Irregular White Space",-1,9,sPos-wsLength,sPos);
                }
                //TODO closing brace
            }
        }
    }

    /**
     * lint Service Constructor node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintServiceConstructorNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.SERVICE)) {
                        if(noHeightAvailable(text))
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                    }else if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                        logError(node, compilationUnitNode, dLog, "Irregular White Space");
                    }
                }
            }
        }
    }


    /**
     * lint Foreach node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintForeachNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Update whitespace for the foreach signature.
            for (JsonElement item : ws) {
                JsonObject wsItem = item.getAsJsonObject();
                if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                    String text = wsItem.get(FormattingConstants.TEXT).getAsString();

                    // Update whitespace for the foreach keyword.
                    if (text.equals(Tokens.FOREACH)) {
                        if(!noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }
                    }

                    // Update whitespace for the opening parentheses.
                    if (text.equals(Tokens.OPENING_PARENTHESES)) {
                        if(!wsItem.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }
                    }

                    // Update whitespace for the param separator.
                    if (text.equals(Tokens.COMMA)) {
                        if(!wsItem.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }
                    }

                    // Update the whitespace for in keyword.
                    if (text.equals(Tokens.IN)) {
                        if(!wsItem.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }
                    }

                    // Update whitespace for the closing parentheses.
                    if (text.equals(Tokens.CLOSING_PARENTHESES)) {
                        if(!wsItem.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }
                    }

                    // Update the whitespace for opening bracket.
                    if (text.equals(Tokens.OPENING_BRACE)) {
                        if(!wsItem.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space");
                        }
                    }

                    // Update the whitespace for closing bracket.
                    if (text.equals(Tokens.CLOSING_BRACE)) {
                        // TODO
                    }
                }
            }

            // Handle whitespace for variables
            if (node.has("variableDefinitionNode")) {
                JsonObject variableDefinitionNode = node.getAsJsonObject("variableDefinitionNode");
                JsonArray variableDefinitions = variableDefinitionNode.getAsJsonArray(FormattingConstants.WS);
                for (int i = 1; i < variableDefinitions.size(); i++) {
                    JsonObject currentWS = variableDefinitions.get(i).getAsJsonObject();
                    if(currentWS.get(FormattingConstants.TEXT).getAsString().equals("var")
                        || currentWS.get(FormattingConstants.TEXT).getAsString().equals("in")
                        || currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.OPENING_BRACE)
                    ){
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE))
                            logError(variableDefinitionNode, compilationUnitNode, dLog, "Irregular White Space");
                    }

                }

                JsonObject variableNode = variableDefinitionNode.getAsJsonObject("variable");
                JsonArray variablesWS = variableNode.getAsJsonArray(FormattingConstants.WS);
                for (int i = 0; i < variablesWS.size(); i++) {
                    JsonObject currentWS = variablesWS.get(i).getAsJsonObject();
                    switch (currentWS.get(FormattingConstants.TEXT).getAsString()) {
                        case Tokens.OPENING_BRACKET:
                            if (!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE))
                                logError(variableDefinitionNode, compilationUnitNode, dLog, "Irregular White Space");

                            break;
                        case Tokens.COMMA:
                            if (!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE))
                                logError(variableDefinitionNode, compilationUnitNode, dLog, "Irregular White Space");

                            break;
                        case Tokens.CLOSING_BRACKET:
                            if (!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE))
                                logError(variableDefinitionNode, compilationUnitNode, dLog, "Irregular White Space");
                            break;
                    }

                }

                JsonArray variables = variableNode.getAsJsonArray("variables");
                for(int i=0; i< variables.size(); i++){
                    JsonArray VariableWS = variables.get(i).getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray();
                    JsonObject currentWS = VariableWS.get(0).getAsJsonObject();
                        if(i==0){
                            if (!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) logError(variableDefinitionNode, compilationUnitNode, dLog, "Irregular White Space");

                        }else if (!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                            logError(variableDefinitionNode, compilationUnitNode, dLog, "Irregular White Space");
                        }


                }
            }
        }
    }

    /**
     * lint Expression Statement node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintExpressionStatementNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Update whitespaces for expression.
            //TODO
//            if (node.has(FormattingConstants.EXPRESSION)) {
//                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
//            }

            // Update whitespace for semicolon.
            JsonObject semicolonWhitespace = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
                if(!semicolonWhitespace.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                    logError(node, compilationUnitNode, dLog, "Irregular White Space");
                }
            }
        }
    }

    /**
     * lint Error type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintErrorTypeNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.ERROR)) {

                    } else if (text.equals(Tokens.LESS_THAN) || text.equals(Tokens.COMMA)
                            || text.equals(Tokens.GREATER_THAN) || text.equals(Tokens.QUESTION_MARK)) {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near '?'");
                        }
                    }
                }
            }

            // Handle formatting for reason type node.
            if (node.has("reasonTypeNode")) {
                //TODO
            }

            // Handle formatting for details type node.
            if (node.has("detailsTypeNode")) {

            }
        }
    }

    /**
     * lint Error Destructure node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintAssignmentNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate and update whitespaces for error destructure node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.EQUAL)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.SEMICOLON)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Handle error variable reference formatting.
            if (node.has("varRef")) {
                node.getAsJsonObject("varRef").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            // Handle expression formatting.
            if (node.has(FormattingConstants.EXPRESSION)) {

            }
        }
    }

    /**
     * lint Transaction node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintTransactionNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Update transaction and retry whitespaces.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                int startLine = node.get(FormattingConstants.POSITION).getAsJsonObject().get("startLine").getAsInt();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.TRANSACTION)) {
                        //TODO
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.ONRETRY)) {

                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.OPENING_BRACE)) {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near '{'",startLine,startLine,-1,-1);
                        }
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.CLOSING_BRACE)) {
                        //TODO
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.WITH)) {

                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.RETRIES)) {

                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.EQUAL)) {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near '='",startLine,startLine,-1,-1);
                        }
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.COMMA)) {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near ','");
                        }
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.ONABORT)) {

                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.ONCOMMIT)) {

                    }
                }
            }

        }
    }

    /**
     * lint abort node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintAbortNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Update done keyword whitespace.
            JsonObject doneWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(doneWS.get(FormattingConstants.WS).getAsString())) {
                logError(node, compilationUnitNode, dLog, "Should be started in a new line");
            }

            // Update semicolon whitespace.
            JsonObject semicolonWS = ws.get(ws.size() - 1).getAsJsonObject();
            if (this.noHeightAvailable(semicolonWS.get(FormattingConstants.WS).getAsString())) {
                if(!semicolonWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                    logError(node, compilationUnitNode, dLog, "Irregular White Space near ';'");
                }
            }
        }
    }


    /**
     * lint annotation attachment node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintAnnotationAttachmentNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            int sLine = node.get("position").getAsJsonObject().get("startLine").getAsInt();
            int eColumn = -1;
            int SColumn = -1;
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Update whitespace for annotation symbol, @.
            JsonObject annotationSymbolWhitespace = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(annotationSymbolWhitespace.get(FormattingConstants.WS).getAsString())) {
                logError(node, compilationUnitNode, dLog, "Annotation should be started in a new line",sLine,sLine,-1,1);

            }

            // Update whitespace for annotation identifier.
            JsonObject identifierWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (!identifierWhitespace.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                SColumn = getStartPosition(ws,0,ws.size() - 1);
                eColumn = getEndPosition(ws,0,ws.size() - 1);
                logError(node, compilationUnitNode, dLog, "Irregular white space !",sLine,sLine,SColumn,eColumn);
            }

            // Update whitespace for type.
            JsonObject typeWhitespace = ws.get(1).getAsJsonObject();
            if (!typeWhitespace.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                eColumn = getEndPosition(ws,0,1);
                logError(node, compilationUnitNode, dLog, "Irregular white space !",sLine,sLine,-1,eColumn);
            }

            for (int i = 1; i < ws.size() -1; i++) {
                JsonObject currentWs = ws.get(i).getAsJsonObject();
                if (!currentWs.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)) {
                    eColumn = getEndPosition(ws,0,i);
                    SColumn = getStartPosition(ws,0,i);
                    logError(node, compilationUnitNode, dLog, "Irregular white space !",sLine,sLine,SColumn,eColumn);
                }
            }
        }
    }

    /**
     * lint Array Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintArrayTypeNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {

           if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

                for (int i = 0; i < ws.size(); i++) {
                    JsonObject wsItem = ws.get(i).getAsJsonObject();
                    if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                        String text = wsItem.get(FormattingConstants.TEXT).getAsString();
                        if (text.equals(Tokens.OPENING_PARENTHESES) && i == 0) {
                            if(!wsItem.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                                logError(node, compilationUnitNode, dLog, "Irregular White Space near '['");
                            }
                            // Update grouped opening parentheses whitespace.
                            if (node.has(FormattingConstants.GROUPED) &&
                                    node.get(FormattingConstants.GROUPED).getAsBoolean()) {
                                //TODO
                            }
                        } else {
                            // Update rest of the token whitespaces.
                            if (text.equals(Tokens.CLOSING_BRACKET)) {
                                if(!wsItem.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                                    logError(node, compilationUnitNode, dLog, "Irregular White Space near ']'");
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * lint arrow expr node.
     *
     * @param node {JsonObject} node as json object
     */
    //TODO
    public void lintArrowExprNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            for (JsonElement wsItem : ws) {
                JsonObject arrowExprWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(arrowExprWS.get(FormattingConstants.WS).getAsString())) {
                    String text = arrowExprWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.OPENING_PARENTHESES)) {

                    }

                    if (text.equals(Tokens.COMMA)) {

                    }

                    if (text.equals(Tokens.CLOSING_PARENTHESES)) {

                    }

                    if (text.equals(Tokens.EQUAL_GT)) {

                    }
                }
            }

            // Update whitespaces of parameters.
            if (node.has("parameters")) {
                JsonArray parameters = node.getAsJsonArray("parameters");
                boolean hasParentheses = node.has("hasParantheses")
                        && node.get("hasParantheses").getAsBoolean();
                for (int i = 0; i < parameters.size(); i++) {
                    JsonObject param = parameters.get(i).getAsJsonObject();
                    JsonObject paramFormatConfig;
                    // If parentheses available first param should fronted with empty space
                    // Else first param should fronted with space count parent provided.
                    if (i == 0) {


                    } else {


                    }

                }
            }

            // Update whitespace of expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);

            }
        }
    }

    /**
     * lint Binary Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintBinaryExprNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Update the operator symbol whitespace.
            JsonObject operatorSymbolWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(operatorSymbolWS.get(FormattingConstants.WS).getAsString())) {
                if(!operatorSymbolWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                    logError(node, compilationUnitNode, dLog, "Irregular White Space near " + operatorSymbolWS.get(FormattingConstants.TEXT).getAsString());
                }
            }

            //TODO
            // Handle left expression whitespaces.
            if (node.has("leftExpression")) {

            }

            // Handle right expression whitespaces.
            if (node.has("rightExpression")) {
//                JsonObject rightExpression = node.getAsJsonObject("rightExpression");
//                JsonObject operatorRightWS = rightExpression.get(FormattingConstants.WS).getAsJsonArray().get(0).getAsJsonObject();
//                if (this.noHeightAvailable(operatorRightWS.get(FormattingConstants.WS).getAsString())) {
//                    if(!operatorRightWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
//                        logError(node, compilationUnitNode, dLog, "Irregular White Space near " + operatorSymbolWS.get(FormattingConstants.TEXT).getAsString());
//                    }
//                }
            }
        }
    }

    /**
     * lint Block node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintBlockNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        JsonObject position = new JsonObject();

        // Update the statements whitespaces.
        for (int i = 0; i < node.getAsJsonArray(FormattingConstants.STATEMENTS).size(); i++) {
            JsonElement child = node.getAsJsonArray(FormattingConstants.STATEMENTS).get(i);


        }

        // If this is a else block continue to following.
        if (node.has(FormattingConstants.WS) && node.getAsJsonArray(FormattingConstants.WS).get(0).getAsJsonObject()
                .get(FormattingConstants.TEXT).getAsString().equals(Tokens.ELSE)) {

            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Update the else keyword whitespace.
            JsonObject elseKeywordWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(elseKeywordWS.get(FormattingConstants.WS).getAsString())) {
                if(!elseKeywordWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                    logError(node, compilationUnitNode, dLog, "Irregular White Space near 'else'");
                }
            }

            // Update the opening brace whitespace.
            JsonObject openingBraceWS = ws.get(ws.size() - 2).getAsJsonObject();
            if (this.noHeightAvailable(openingBraceWS.get(FormattingConstants.WS).getAsString())) {
                if(!openingBraceWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                    logError(node, compilationUnitNode, dLog, "Irregular White Space near '{'");
                }
            }

            // Update the closing brace whitespace.
            //TODO
            JsonObject closingBraceWS = ws.get(ws.size() - 1).getAsJsonObject();
            if (node.getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0) {
                if (this.noHeightAvailable(closingBraceWS.get(FormattingConstants.WS).getAsString())) {

                }
            } else if (this.noHeightAvailable(closingBraceWS.get(FormattingConstants.WS).getAsString())) {

            }
        }
    }

    /**
     * lint built in ref type.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintBuiltInRefTypeNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Update the ref type whitespace.
            JsonObject refTypeWhitespace = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(refTypeWhitespace.get(FormattingConstants.WS).getAsString())) {
                if(!refTypeWhitespace.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                    logError(node, compilationUnitNode, dLog, "Irregular White Space");
                }
            }
        }
    }

    /**
     * lint Compound Assignment node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintCompoundAssignmentNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            String compoundOperator = node.get("operatorKind").getAsString();

            // Iterate and format compound assignment whitespaces.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.contains(compoundOperator)) {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ text);
                        }
                    } else if (text.equals(Tokens.SEMICOLON)) {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ text);
                        }
                    }
                }
            }
        }
    }

    /**
     * lint markdown documentation node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintMarkdownDocumentationNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Iterate through whitespaces and update accordingly.
            for (int i = 0; i < ws.size(); i++) {
                JsonObject currentWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals("#")) {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ text);
                        }
                    }
                }
            }

            //TODO
            if (node.has("parameters")
                    && node.getAsJsonArray("parameters").size() > 0) {
                JsonArray parameters = node.getAsJsonArray("parameters");
                for (JsonElement parameter : parameters) {

                }
            }

            if (node.has("returnParameter")) {
                JsonObject returnParameter = node.getAsJsonObject("returnParameter");

            }
        }
    }

    /**
     * lint Field Based Access Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintFieldBasedAccessExprNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Update rest of the whitespaces.
            for (JsonElement jsonElement : ws) {
                if (this.noHeightAvailable(jsonElement.getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                    if(!jsonElement.getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                        logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ jsonElement.getAsJsonObject().get(FormattingConstants.TEXT).getAsString());
                    }
                }
            }
        }
    }

    /**
     * lint group expression node.
     *
     * @param node {@link JsonObject} node as a json object
     */
    public void formatGroupExprNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Iterate and update whitespaces.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.OPENING_PARENTHESES)) {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ text);
                        }
                    } else if (text.equals(Tokens.CLOSING_PARENTHESES)) {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ text);
                        }
                    }
                }
            }
        }
    }

    /**
     * lint Identifier node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintIdentifierNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            // Iterate and update whitespaces for the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.EQUAL)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ text);
                        }
                    } else {
                        //TODO
                    }
                }
            }
        }
    }

    /**
     * format Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintLiteralNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        this.lintLiteral(node,compilationUnitNode,dLog);;
    }

    /**
     * format Numeric Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintNumericLiteralNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        this.lintLiteral(node,compilationUnitNode,dLog);;
    }

    private void lintLiteral(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            for (int i = 0; i < ws.size(); i++) {
                JsonObject currentWS = ws.get(i).getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();

                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (i == 0) {
                        //TODO
                    } else {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ text);
                        }
                    }
                }
            }
        }
    }

    /**
     * lint Record Literal Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintRecordLiteralExprNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            String parentKind = node.getAsJsonObject(FormattingConstants.PARENT).get(FormattingConstants.KIND)
                    .getAsString();
            boolean isTable = parentKind.equals("Table");

            // Has at least one line separation in records.
            boolean lineSeparationAvailable = false;
            if (node.has("keyValuePairs")) {
                lineSeparationAvailable = this.isMemberOnNewLine(node.getAsJsonArray("keyValuePairs"));
            }

            // Iterate and update Whitespaces for the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                // Update whitespace for opening brace.
                if (text.equals(Tokens.OPENING_BRACE)
                        && this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (isTable) {
                        //TODO
                    } else {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.SINGLE_SPACE)){
                            int sLine = node.get("position").getAsJsonObject().get("startLine").getAsInt();
                            int sCol = node.get("position").getAsJsonObject().get("startColumn").getAsInt();
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ text, -1, sLine, -1, sCol+1);
                        }
                    }
                }

                // lint whitespace for closing brace.
                if (text.equals(Tokens.CLOSING_BRACE)
                        && this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (lineSeparationAvailable) {
                        if (node.has("keyValuePairs")
                                && node.getAsJsonArray("keyValuePairs").size() <= 0) {
                            if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                                logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ text);
                            }
                        } else {
                            //TODO
                        }
                    } else {
                        if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                            logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ text);
                        }
                    }
                }

                // lint whitespaces for the key value pair separator , or ;.
                if (text.equals(Tokens.COMMA) ||
                        currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.SEMICOLON)) {
                    if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                        logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ text);
                    }
                }
            }

            // lint whitespace for colon of the record literal key value pair.
            if (node.has("keyValuePairs")) {
                JsonArray keyValuePairs = node.getAsJsonArray("keyValuePairs");
                for (int i = 0; i < keyValuePairs.size(); i++) {
                    JsonObject currentWS = keyValuePairs.get(i).getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray().get(0).getAsJsonObject();
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if(!currentWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                        logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ text);
                    }

                }
            }
        }
    }

    /**
     * lint record literal key value.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintRecordLiteralKeyValueNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Update whitespace for key value of record literal.
            if (node.has("key")) {
                JsonObject keyNode = node.getAsJsonObject("key");
                //TODO
            }

            // lint whitespace for value of record literal.
            if (node.has(FormattingConstants.VALUE)) {
                JsonObject valueNode = node.getAsJsonObject(FormattingConstants.VALUE);
                JsonObject valueWS = valueNode.get(FormattingConstants.WS).getAsJsonArray().get(0).getAsJsonObject();
                if(!valueWS.get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                    logError(node, compilationUnitNode, dLog, "Irregular White Space near "+ valueWS.get(FormattingConstants.TEXT));
                }
            }
        }
    }

    /**
     * lint Simple Variable Ref node.
     *
     * @param node {JsonObject} node as json object
     */
    public void lintSimpleVariableRefNode(JsonObject node, Node compilationUnitNode, DiagnosticLog dLog) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String packageAlias = "";
            if (node.has("packageAlias")) {
                packageAlias = node.getAsJsonObject("packageAlias").get("value").getAsString();
            }

            int colonIndex = 0;
            boolean packageAliasAvailableBeforeColon = false;
            for (int i = 0; i < ws.size(); i++) {
                JsonObject currentWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();

                    if (i == 0 && !text.equals(Tokens.COLON)) {
                        if (text.equals(packageAlias)) {
                            packageAliasAvailableBeforeColon = true;
                        }
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(Tokens.COLON)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        ++colonIndex;
                    } else {
                        if (colonIndex == 1) {
                            if (packageAliasAvailableBeforeColon) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            }
                        } else if (colonIndex > 1) {


                        } else {
                            if (ws.size() > 1) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean noHeightAvailable(String ws) {
        return ws.trim().length() <= 0 && !ws.contains("\n");
    }

    private int getStartPosition(JsonArray ws, int from, int to){
        int count = 0;

        for (int i = from; i < to; i++) {
            String text = ws.get(i).getAsJsonObject().get(FormattingConstants.TEXT).getAsString();

            count += text.length();
        }
        for (int i = from; i < to; i++) {
            String wsStr = ws.get(i).getAsJsonObject().get(FormattingConstants.WS).getAsString().replace("\n", "");

            count += getWhiteSpaceCount(wsStr);
        }
        return ++count;
    }
    private int getEndPosition(JsonArray ws, int from, int to){
        int count = 0;

        for (int i = from; i < to; i++) {
            String text = ws.get(i).getAsJsonObject().get(FormattingConstants.TEXT).getAsString();

            count += text.length();
        }
        for (int i = from; i <= to; i++) {
            String wsStr = ws.get(i).getAsJsonObject().get(FormattingConstants.WS).getAsString().replace("\n", "");

            count += getWhiteSpaceCount(wsStr);
        }
        return ++count;
    }

    private String getWhiteSpaces(int column) {
        StringBuilder whiteSpaces = new StringBuilder();
        for (int i = 0; i <= (column - 1); i++) {
            whiteSpaces.append(" ");
        }

        return whiteSpaces.toString();
    }

    private int getWhiteSpaceCount(String ws) {
        return ws.length();
    }

    private void preserveHeight(JsonArray ws, String indent) {
        for (int i = 0; i < ws.size(); i++) {
            if (ws.get(i).isJsonObject()) {
                preserveHeightForWS(ws.get(i).getAsJsonObject(), indent);
            }
        }
    }

    private void preserveHeightForWS(JsonObject ws, String indent) {
        if (ws.has(FormattingConstants.WS) &&
                (ws.get(FormattingConstants.WS).getAsString().trim().length() > 0 ||
                        ws.get(FormattingConstants.WS).getAsString().contains("\n"))) {
            List<String> tokens = this.tokenizer(ws.get(FormattingConstants.WS).getAsString());
            ws.addProperty(FormattingConstants.WS,
                    this.getTextFromTokens(tokens, indent));
        }
    }

    private List<String> tokenizer(String text) {
        List<String> tokens = new ArrayList<>();
        StringBuilder comment = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            String character = text.charAt(i) + "";
            if (!character.contains("\n")) {
                comment.append(text.charAt(i));
            } else {
                if (!comment.toString().trim().equals("")) {
                    tokens.add(comment.toString().trim());
                    comment = new StringBuilder();
                }
                tokens.add(character);
            }

            if (i == (text.length() - 1) && !comment.toString().trim().equals("")) {
                tokens.add(comment.toString().trim());
                comment = new StringBuilder();
            }
        }
        return tokens;
    }

    private String getTextFromTokens(List<String> tokens, String indent) {
        StringBuilder text = new StringBuilder();
        for (String token : tokens) {
            if (!token.contains("\n")) {
                text.append(indent != null ? indent + token : token);
            } else {
                text.append(token);
            }
        }

        return indent != null ? (text + indent) : text.toString();
    }

    private String getIndentation(JsonObject formatConfig, boolean addSpaces) {
        String indentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                FormattingConstants.SPACE_TAB)
                : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt());

        // If add space is true add spaces to the
        // indentation as provided by the format config
        return addSpaces ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt())
                + indentation : indentation;
    }

    private String getParentIndentation(JsonObject formatConfig) {
        return formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                FormattingConstants.SPACE_TAB
                : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());
    }

    private boolean noNewLine(String text) {
        return !text.contains("\n");
    }

    private boolean isMemberOnNewLine(JsonArray members) {
        boolean lineSeparationAvailable = false;
        for (JsonElement memberItem : members) {
            JsonObject member = memberItem.getAsJsonObject();
            if (member.has(FormattingConstants.WS)) {
                List<JsonObject> sortedWSForMember = FormattingSourceGen.extractWS(member);
                for (JsonObject wsForMember : sortedWSForMember) {
                    String currentWS = wsForMember.get(FormattingConstants.WS).getAsString();
                    if (!noNewLine(currentWS)) {
                        lineSeparationAvailable = true;
                        break;
                    }
                }
            }
        }
        return lineSeparationAvailable;
    }

    private int findStartPosition(String text, String token){
        return text.split(token)[0].length();
    }
}

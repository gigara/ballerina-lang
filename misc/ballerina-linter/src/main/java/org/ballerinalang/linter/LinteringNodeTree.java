package org.ballerinalang.linter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.compiler.format.FormattingConstants;
import org.ballerinalang.langserver.compiler.format.Tokens;
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
     * format Service node.
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
                            logError(node.get("name").getAsJsonObject(), compilationUnitNode, dLog, "Irregular whitespace near the 'service' keyword");
                        }
                    }
                }

                // Update whitespaces for body.
                if (node.has("typeDefinition")
                        && node.getAsJsonObject("typeDefinition").has(FormattingConstants.TYPE_NODE)) {
                    JsonObject typeNode = node.getAsJsonObject("typeDefinition").getAsJsonObject(FormattingConstants.TYPE_NODE);

                    //parameteres
                    JsonArray parameters = typeNode.get("functions").getAsJsonObject().get("paramenters").getAsJsonArray();

                    for(int i=0; i<parameters.size(); i++){
                        JsonArray parameterAttr = parameters.get(i).getAsJsonObject().get(FormattingConstants.TYPE_NODE).getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray();
                        JsonObject position = parameters.get(i).getAsJsonObject().get(FormattingConstants.TYPE_NODE).getAsJsonObject().get("position").getAsJsonObject();
                        for(int j=0; j<parameterAttr.size(); j++){
                            if(!parameterAttr.get(j).getAsJsonObject().get(FormattingConstants.WS).getAsString().equals(FormattingConstants.EMPTY_SPACE)){
                                logError(position, compilationUnitNode, dLog, "Irregular whitespace");
                            }
                        }
                    }
                }

                // Update whitespaces for body.
                if (node.has("typeDefinition")
                        && node.getAsJsonObject("typeDefinition").has(FormattingConstants.TYPE_NODE)) {
                    JsonObject typeNode = node.getAsJsonObject("typeDefinition")
                            .getAsJsonObject(FormattingConstants.TYPE_NODE);

                }

                // Update whitespaces for resources.
                if (node.has("resources")) {
                    JsonArray resources = node.getAsJsonArray("resources");

                }

                // Update whitespaces of markdown documentation attachments.
              //  modifyMarkdownDocumentation(node, formatConfig, indentation);

                // Update whitespaces of the annotation attachments.
              //  modifyAnnotationAttachments(node, formatConfig, indentation);

                // Handles formatting for attached expressions.
                if (node.has("attachedExprs")) {
                    JsonArray attachedExprs = node.getAsJsonArray("attachedExprs");
                    for (JsonElement attachedExpr : attachedExprs) {

                    }
                }
            }
        }
    }

    private boolean noHeightAvailable(String ws) {
        return ws.trim().length() <= 0 && !ws.contains("\n");
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

}

package org.ballerinalang.langserver.compiler.gigaFormat;

import io.ballerinalang.compiler.syntax.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FormattingTransformer extends NodeTransformer<Node> {
    private Stack<Node> otherTopLevelNodes = new Stack<>();

    public List<Node> accept(Node node) {
        Node visitNode = node.apply(this);
        List<Node> nodes = new ArrayList<>();
        while (!otherTopLevelNodes.empty()) {
            nodes.add(otherTopLevelNodes.pop());
        }
        nodes.add(visitNode);
        return nodes;
    }

    @Override
    protected Node transformSyntaxNode(Node node) {
        return node;
    }

    @Override
    public Node transform(ModulePartNode modulePartNode) {
        NodeList<ImportDeclarationNode> imports = modulePartNode.imports();
        NodeList<ModuleMemberDeclarationNode> members = formatNodeList(modulePartNode.members());
        Token eofToken = modulePartNode.eofToken();
        return modulePartNode.modify(
                imports,
                members,
                eofToken);
    }

    @Override
    public Node transform(FunctionDefinitionNode functionDefinitionNode) {
        Token functionKeyword = functionDefinitionNode.functionKeyword();

        MinutiaeList leadingMinutiaeList = functionKeyword.leadingMinutiae();
        MinutiaeList trailingMinutiaeList = functionKeyword.trailingMinutiae();

        return functionDefinitionNode
                .modify()
                .withFunctionKeyword(functionKeyword)
                .apply();
    }

    @Override
    public Node transform(ImportDeclarationNode importDeclarationNode) {
        return super.transform(importDeclarationNode);
    }

    @Override
    public Node transform(ListenerDeclarationNode listenerDeclarationNode) {
        return super.transform(listenerDeclarationNode);
    }

    @Override
    public Node transform(TypeDefinitionNode typeDefinitionNode) {
        return super.transform(typeDefinitionNode);
    }

    @Override
    public Node transform(ServiceDeclarationNode serviceDeclarationNode) {
        return super.transform(serviceDeclarationNode);
    }

    @Override
    public Node transform(AssignmentStatementNode assignmentStatementNode) {
        return super.transform(assignmentStatementNode);
    }

    @Override
    public Node transform(CompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        return super.transform(compoundAssignmentStatementNode);
    }

    @Override
    public Node transform(VariableDeclarationNode variableDeclarationNode) {
        return super.transform(variableDeclarationNode);
    }

    @Override
    public Node transform(BlockStatementNode blockStatementNode) {
        return super.transform(blockStatementNode);
    }

    @Override
    public Node transform(BreakStatementNode breakStatementNode) {
        return super.transform(breakStatementNode);
    }

    @Override
    public Node transform(ExpressionStatementNode expressionStatementNode) {
        return super.transform(expressionStatementNode);
    }

    @Override
    public Node transform(ContinueStatementNode continueStatementNode) {
        return super.transform(continueStatementNode);
    }

    @Override
    public Node transform(ExternalFunctionBodyNode externalFunctionBodyNode) {
        return super.transform(externalFunctionBodyNode);
    }

    @Override
    public Node transform(IfElseStatementNode ifElseStatementNode) {
        return super.transform(ifElseStatementNode);
    }

    @Override
    public Node transform(ElseBlockNode elseBlockNode) {
        return super.transform(elseBlockNode);
    }

    @Override
    public Node transform(WhileStatementNode whileStatementNode) {
        return super.transform(whileStatementNode);
    }

    @Override
    public Node transform(PanicStatementNode panicStatementNode) {
        return super.transform(panicStatementNode);
    }

    @Override
    public Node transform(ReturnStatementNode returnStatementNode) {
        return super.transform(returnStatementNode);
    }

    @Override
    public Node transform(LocalTypeDefinitionStatementNode localTypeDefinitionStatementNode) {
        return super.transform(localTypeDefinitionStatementNode);
    }

    @Override
    public Node transform(LockStatementNode lockStatementNode) {
        return super.transform(lockStatementNode);
    }

    @Override
    public Node transform(ForkStatementNode forkStatementNode) {
        return super.transform(forkStatementNode);
    }

    @Override
    public Node transform(ForEachStatementNode forEachStatementNode) {
        return super.transform(forEachStatementNode);
    }

    @Override
    public Node transform(BinaryExpressionNode binaryExpressionNode) {
        return super.transform(binaryExpressionNode);
    }

    @Override
    public Node transform(BracedExpressionNode bracedExpressionNode) {
        return super.transform(bracedExpressionNode);
    }

    @Override
    public Node transform(CheckExpressionNode checkExpressionNode) {
        return super.transform(checkExpressionNode);
    }

    @Override
    public Node transform(FieldAccessExpressionNode fieldAccessExpressionNode) {
        return super.transform(fieldAccessExpressionNode);
    }

    @Override
    public Node transform(FunctionCallExpressionNode functionCallExpressionNode) {
        return super.transform(functionCallExpressionNode);
    }

    @Override
    public Node transform(MethodCallExpressionNode methodCallExpressionNode) {
        return super.transform(methodCallExpressionNode);
    }

    @Override
    public Node transform(MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        return super.transform(mappingConstructorExpressionNode);
    }

    @Override
    public Node transform(IndexedExpressionNode indexedExpressionNode) {
        return super.transform(indexedExpressionNode);
    }

    @Override
    public Node transform(TypeofExpressionNode typeofExpressionNode) {
        return super.transform(typeofExpressionNode);
    }

    @Override
    public Node transform(UnaryExpressionNode unaryExpressionNode) {
        return super.transform(unaryExpressionNode);
    }

    @Override
    public Node transform(ComputedNameFieldNode computedNameFieldNode) {
        return super.transform(computedNameFieldNode);
    }

    @Override
    public Node transform(ConstantDeclarationNode constantDeclarationNode) {
        return super.transform(constantDeclarationNode);
    }

    @Override
    public Node transform(DefaultableParameterNode defaultableParameterNode) {
        return super.transform(defaultableParameterNode);
    }

    @Override
    public Node transform(RequiredParameterNode requiredParameterNode) {
        return super.transform(requiredParameterNode);
    }

    @Override
    public Node transform(RestParameterNode restParameterNode) {
        return super.transform(restParameterNode);
    }

    @Override
    public Node transform(ExpressionListItemNode expressionListItemNode) {
        return super.transform(expressionListItemNode);
    }

    @Override
    public Node transform(ImportOrgNameNode importOrgNameNode) {
        return super.transform(importOrgNameNode);
    }

    @Override
    public Node transform(ImportPrefixNode importPrefixNode) {
        return super.transform(importPrefixNode);
    }

    @Override
    public Node transform(ImportSubVersionNode importSubVersionNode) {
        return super.transform(importSubVersionNode);
    }

    @Override
    public Node transform(ImportVersionNode importVersionNode) {
        return super.transform(importVersionNode);
    }

    @Override
    public Node transform(SpecificFieldNode specificFieldNode) {
        return super.transform(specificFieldNode);
    }

    @Override
    public Node transform(SpreadFieldNode spreadFieldNode) {
        return super.transform(spreadFieldNode);
    }

    @Override
    public Node transform(NamedArgumentNode namedArgumentNode) {
        return super.transform(namedArgumentNode);
    }

    @Override
    public Node transform(PositionalArgumentNode positionalArgumentNode) {
        return super.transform(positionalArgumentNode);
    }

    @Override
    public Node transform(RestArgumentNode restArgumentNode) {
        return super.transform(restArgumentNode);
    }

    @Override
    public Node transform(ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        return super.transform(objectTypeDescriptorNode);
    }

    @Override
    public Node transform(RecordTypeDescriptorNode recordTypeDescriptorNode) {
        return super.transform(recordTypeDescriptorNode);
    }

    @Override
    public Node transform(ReturnTypeDescriptorNode returnTypeDescriptorNode) {
        return super.transform(returnTypeDescriptorNode);
    }

    @Override
    public Node transform(NilTypeDescriptorNode nilTypeDescriptorNode) {
        return super.transform(nilTypeDescriptorNode);
    }

    @Override
    public Node transform(OptionalTypeDescriptorNode optionalTypeDescriptorNode) {
        return super.transform(optionalTypeDescriptorNode);
    }

    @Override
    public Node transform(ObjectFieldNode objectFieldNode) {
        return super.transform(objectFieldNode);
    }

    @Override
    public Node transform(RecordFieldNode recordFieldNode) {
        return super.transform(recordFieldNode);
    }

    @Override
    public Node transform(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        return super.transform(recordFieldWithDefaultValueNode);
    }

    @Override
    public Node transform(RecordRestDescriptorNode recordRestDescriptorNode) {
        return super.transform(recordRestDescriptorNode);
    }

    @Override
    public Node transform(TypeReferenceNode typeReferenceNode) {
        return super.transform(typeReferenceNode);
    }

    @Override
    public Node transform(ServiceBodyNode serviceBodyNode) {
        return super.transform(serviceBodyNode);
    }

    @Override
    public Node transform(AnnotationNode annotationNode) {
        return super.transform(annotationNode);
    }

    @Override
    public Node transform(MetadataNode metadataNode) {
        return super.transform(metadataNode);
    }

    @Override
    public Node transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        return super.transform(moduleVariableDeclarationNode);
    }

    @Override
    public Node transform(TypeTestExpressionNode typeTestExpressionNode) {
        return super.transform(typeTestExpressionNode);
    }

    @Override
    public Node transform(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        return super.transform(remoteMethodCallActionNode);
    }

    @Override
    public Node transform(ParameterizedTypeDescriptorNode parameterizedTypeDescriptorNode) {
        return super.transform(parameterizedTypeDescriptorNode);
    }

    @Override
    public Node transform(NilLiteralNode nilLiteralNode) {
        return super.transform(nilLiteralNode);
    }

    @Override
    public Node transform(AnnotationDeclarationNode annotationDeclarationNode) {
        return super.transform(annotationDeclarationNode);
    }

    @Override
    public Node transform(AnnotationAttachPointNode annotationAttachPointNode) {
        return super.transform(annotationAttachPointNode);
    }

    @Override
    public Node transform(XMLNamespaceDeclarationNode xMLNamespaceDeclarationNode) {
        return super.transform(xMLNamespaceDeclarationNode);
    }

    @Override
    public Node transform(FunctionBodyBlockNode functionBodyBlockNode) {
        return super.transform(functionBodyBlockNode);
    }

    @Override
    public Node transform(NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        return super.transform(namedWorkerDeclarationNode);
    }

    @Override
    public Node transform(NamedWorkerDeclarator namedWorkerDeclarator) {
        return super.transform(namedWorkerDeclarator);
    }

    @Override
    public Node transform(DocumentationStringNode documentationStringNode) {
        return super.transform(documentationStringNode);
    }

    @Override
    public Node transform(BasicLiteralNode basicLiteralNode) {
        return super.transform(basicLiteralNode);
    }

    @Override
    public Node transform(SimpleNameReferenceNode simpleNameReferenceNode) {
        return super.transform(simpleNameReferenceNode);
    }

    @Override
    public Node transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        return super.transform(qualifiedNameReferenceNode);
    }

    @Override
    public Node transform(BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        return super.transform(builtinSimpleNameReferenceNode);
    }

    @Override
    public Node transform(TrapExpressionNode trapExpressionNode) {
        return super.transform(trapExpressionNode);
    }

    @Override
    public Node transform(ListConstructorExpressionNode listConstructorExpressionNode) {
        return super.transform(listConstructorExpressionNode);
    }

    @Override
    public Node transform(TypeCastExpressionNode typeCastExpressionNode) {
        return super.transform(typeCastExpressionNode);
    }

    @Override
    public Node transform(TypeCastParamNode typeCastParamNode) {
        return super.transform(typeCastParamNode);
    }

    @Override
    public Node transform(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        return super.transform(unionTypeDescriptorNode);
    }

    @Override
    public Node transform(TableConstructorExpressionNode tableConstructorExpressionNode) {
        return super.transform(tableConstructorExpressionNode);
    }

    @Override
    public Node transform(KeySpecifierNode keySpecifierNode) {
        return super.transform(keySpecifierNode);
    }

    @Override
    public Node transform(ErrorTypeDescriptorNode errorTypeDescriptorNode) {
        return super.transform(errorTypeDescriptorNode);
    }

    @Override
    public Node transform(ErrorTypeParamsNode errorTypeParamsNode) {
        return super.transform(errorTypeParamsNode);
    }

    @Override
    public Node transform(StreamTypeDescriptorNode streamTypeDescriptorNode) {
        return super.transform(streamTypeDescriptorNode);
    }

    @Override
    public Node transform(StreamTypeParamsNode streamTypeParamsNode) {
        return super.transform(streamTypeParamsNode);
    }

    @Override
    public Node transform(LetExpressionNode letExpressionNode) {
        return super.transform(letExpressionNode);
    }

    @Override
    public Node transform(LetVariableDeclarationNode letVariableDeclarationNode) {
        return super.transform(letVariableDeclarationNode);
    }

    @Override
    public Node transform(TemplateExpressionNode templateExpressionNode) {
        return super.transform(templateExpressionNode);
    }

    @Override
    public Node transform(XMLElementNode xMLElementNode) {
        return super.transform(xMLElementNode);
    }

    @Override
    public Node transform(XMLStartTagNode xMLStartTagNode) {
        return super.transform(xMLStartTagNode);
    }

    @Override
    public Node transform(XMLEndTagNode xMLEndTagNode) {
        return super.transform(xMLEndTagNode);
    }

    @Override
    public Node transform(XMLSimpleNameNode xMLSimpleNameNode) {
        return super.transform(xMLSimpleNameNode);
    }

    @Override
    public Node transform(XMLQualifiedNameNode xMLQualifiedNameNode) {
        return super.transform(xMLQualifiedNameNode);
    }

    @Override
    public Node transform(XMLEmptyElementNode xMLEmptyElementNode) {
        return super.transform(xMLEmptyElementNode);
    }

    @Override
    public Node transform(InterpolationNode interpolationNode) {
        return super.transform(interpolationNode);
    }

    @Override
    public Node transform(XMLTextNode xMLTextNode) {
        return super.transform(xMLTextNode);
    }

    @Override
    public Node transform(XMLAttributeNode xMLAttributeNode) {
        return super.transform(xMLAttributeNode);
    }

    @Override
    public Node transform(XMLAttributeValue xMLAttributeValue) {
        return super.transform(xMLAttributeValue);
    }

    @Override
    public Node transform(XMLComment xMLComment) {
        return super.transform(xMLComment);
    }

    @Override
    public Node transform(XMLProcessingInstruction xMLProcessingInstruction) {
        return super.transform(xMLProcessingInstruction);
    }

    @Override
    public Node transform(FunctionTypeDescriptorNode functionTypeDescriptorNode) {
        return super.transform(functionTypeDescriptorNode);
    }

    @Override
    public Node transform(FunctionSignatureNode functionSignatureNode) {
        return super.transform(functionSignatureNode);
    }

    @Override
    public Node transform(Token token) {
        return super.transform(token);
    }

    @Override
    public Node transform(IdentifierToken identifier) {
        return super.transform(identifier);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    protected <T extends Node> NodeList<T> formatNodeList(NodeList<T> nodeList) {
        for (T t : nodeList) {
            t.apply(this);
        }
        return nodeList;
    }
}

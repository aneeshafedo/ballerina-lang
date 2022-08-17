/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.componentmodel.servicemodel.nodevisitors;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.componentmodel.ComponentModelingConstants.ParameterIn;
import io.ballerina.componentmodel.servicemodel.components.Parameter;
import io.ballerina.componentmodel.servicemodel.components.Resource;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Visitor class for FunctionDefinition node.
 */
public class ResourceAccessorDefinitionNodeVisitor extends NodeVisitor {
    private final String serviceId;
    List<Resource> resources = new ArrayList<>();

    public ResourceAccessorDefinitionNodeVisitor(String serviceId) {
        this.serviceId = serviceId;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        SyntaxKind kind = functionDefinitionNode.kind();
        if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
            StringBuilder resourcePathBuilder = new StringBuilder();
            NodeList<Node> relativeResourcePaths = functionDefinitionNode.relativeResourcePath();
            for (Node path : relativeResourcePaths) {
                resourcePathBuilder.append(path.toString());
            }
            String resourcePath = resourcePathBuilder.toString().trim();
            String method = functionDefinitionNode.functionName().text().trim();

            List<Parameter> parameterList = getParameters(functionDefinitionNode.functionSignature());
            List<String> returnTypes = getReturnTypes(functionDefinitionNode);

            RemoteMethodCallActionNodeVisitor remoteExpressionVisitor = new RemoteMethodCallActionNodeVisitor();
            functionDefinitionNode.accept(remoteExpressionVisitor);

            Resource.ResourceId resourceId = new Resource.ResourceId(this.serviceId, method, resourcePath);

            Resource resource = new Resource(resourceId, parameterList, returnTypes,
                    remoteExpressionVisitor.getInteractionList());
            resources.add(resource);

        }
    }


    private List<String> getReturnTypes(FunctionDefinitionNode functionDefinitionNode) {
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptor = functionSignature.returnTypeDesc();
        if (returnTypeDescriptor.isPresent()) {
            ReturnTypeDescriptorNode returnNode = returnTypeDescriptor.get();
            // need to split by pipe sign
            return Arrays.asList(returnNode.type().toString().trim().split("\\|"));
        }
        return new ArrayList<>();
    }

    private List<Parameter> getParameters(FunctionSignatureNode functionSignatureNode) {
        List<Parameter> parameterList = new ArrayList<>();
        SeparatedNodeList<ParameterNode> parameterNodes = functionSignatureNode.parameters();
        for (ParameterNode parameterNode : parameterNodes) {
            switch (parameterNode.kind()) {
                case REQUIRED_PARAM:
                    RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                    String requiredParamIn = getParameterIn(requiredParameterNode.annotations());
                    String requiredParamName = requiredParameterNode.paramName().isPresent() ?
                            requiredParameterNode.paramName().get().toString() : "";
                    parameterList.add(new Parameter(requiredParameterNode.typeName().toString().trim(),
                            requiredParamName.trim(), requiredParamIn, true));
                    break;
                case DEFAULTABLE_PARAM:
                    DefaultableParameterNode defaultableParameterNode = (DefaultableParameterNode) parameterNode;
                    String defaultableParamIn = getParameterIn(defaultableParameterNode.annotations());
                    String defaultableParamName = defaultableParameterNode.paramName().isPresent() ?
                            defaultableParameterNode.paramName().get().toString() : "";
                    parameterList.add(new Parameter(defaultableParameterNode.typeName().toString().trim(),
                            defaultableParamName.trim(), defaultableParamIn, false));
                    break;
                case INCLUDED_RECORD_PARAM:
                    break;
                // res params

            }

        }
        return parameterList;
    }

    private String getParameterIn(NodeList<AnnotationNode> annotationNodes) {
        String in = "";
        Optional<AnnotationNode> payloadAnnotation = annotationNodes.stream().filter(annot ->
                annot.toString().trim().equals("@http:Payload")).findAny();
        Optional<AnnotationNode> headerAnnotation = annotationNodes.stream().filter(annot ->
                annot.toString().trim().equals("@http:Header")).findAny();
        // do we need to handle http:Request

        if (payloadAnnotation.isPresent()) {
            in = ParameterIn.BODY.getValue();
        } else if (headerAnnotation.isPresent()) {
            in = ParameterIn.HEADER.getValue();
        } else {
            in = ParameterIn.QUERY.getValue();
        }
        return in;
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {

    }

    @Override
    public void visit(ConstantDeclarationNode constantDeclarationNode) {

    }
}

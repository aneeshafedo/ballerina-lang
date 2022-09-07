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
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.Optional;

/**
 * Visitor class to identify client declaration nodes inside a Ballerina service.
 */
public class StatementNodeVisitor extends NodeVisitor {
    private String serviceId;

    private final String clientName;

    public StatementNodeVisitor(String clientName) {
        this.clientName = clientName;
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        if (variableDeclarationNode.typedBindingPattern().bindingPattern().toString().trim().equals(clientName)) {
            NodeList<AnnotationNode> annotations = variableDeclarationNode.annotations();
            this.serviceId = VisitorUtil.getId(annotations);
        }
    }

    @Override
    public void visit(ObjectFieldNode objectFieldNode) {
        if (objectFieldNode.fieldName().text().trim().equals(clientName)) {
            Optional<MetadataNode> metadataNode = objectFieldNode.metadata();
            if (metadataNode.isPresent()) {
                NodeList<AnnotationNode> annotationNodes = metadataNode.get().annotations();
                serviceId = VisitorUtil.getId(annotationNodes);
            }
        }
    }

    @Override
    public void visit(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        if (moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().toString().trim().equals(clientName)) {
            Optional<MetadataNode> metadataNode = moduleVariableDeclarationNode.metadata();
            if (metadataNode.isPresent()) {
                NodeList<AnnotationNode> annotationNodes = metadataNode.get().annotations();
                serviceId = VisitorUtil.getId(annotationNodes);
            }
        }
    }



    @Override
    public void visit(TypeDefinitionNode typeDefinitionNode) {

    }

    @Override
    public void visit(ImportDeclarationNode importDeclarationNode) {

    }

    @Override
    public void visit(EnumDeclarationNode enumDeclarationNode) {

    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {

    }

    @Override
    public void visit(ClassDefinitionNode classDefinitionNode) {

    }


}

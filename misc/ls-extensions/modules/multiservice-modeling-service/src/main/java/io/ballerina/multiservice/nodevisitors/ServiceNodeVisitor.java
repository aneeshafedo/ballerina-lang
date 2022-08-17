package io.ballerina.multiservice.nodevisitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.multiservice.MultiServiceModelingConstants;
import io.ballerina.multiservice.model.ComponentModel;
import io.ballerina.multiservice.model.Service;
import io.ballerina.multiservice.model.entity.Attribute;
import io.ballerina.multiservice.model.entity.Entity;
import io.ballerina.projects.Document;

import java.util.*;

import static io.ballerina.multiservice.MultiServiceModelingConstants.PRIMITIVE_TYPES;

/**
 * Visitor class for ServiceDeclaration nodes.
 */
public class ServiceNodeVisitor extends NodeVisitor {

    private final SemanticModel semanticModel;

    private final Document document;
    private final List<Service> services = new ArrayList<>();

    private final ComponentModel.PackageId packageId;
    private final Map<String, Entity> entities = new HashMap<>();

    public List<Service> getServices() {
        return services;
    }

    public Map<String, Entity> getEntities() {
        return entities;
    }

    public ServiceNodeVisitor(SemanticModel semanticModel, Document document, ComponentModel.PackageId packageId) {
        this.semanticModel = semanticModel;
        this.document = document;
        this.packageId = packageId;
    }

    @Override
    public void visit(ServiceDeclarationNode serviceDeclarationNode) {
        StringBuilder serviceNameBuilder = new StringBuilder();
        String serviceId = "";
        NodeList<Node> serviceNameNodes = serviceDeclarationNode.absoluteResourcePath();
        for (Node serviceNameNode : serviceNameNodes) {
            serviceNameBuilder.append(serviceNameNode.toString());
        }

        String serviceName = serviceNameBuilder.toString().startsWith("/") ?
                serviceNameBuilder.toString().substring(1) : serviceNameBuilder.toString();

        Optional<MetadataNode> metadataNode = serviceDeclarationNode.metadata();
        if (metadataNode.isPresent()) {
            NodeList<AnnotationNode> annotationNodes = metadataNode.get().annotations();
            serviceId = ModelGeneratorUtil.getId(annotationNodes).replace("\"", "").trim();

        }

        ResourceVisitor resourceVisitor = new ResourceVisitor(serviceId, semanticModel, document);
        serviceDeclarationNode.accept(resourceVisitor);
        services.add(new Service(serviceName.trim(), serviceId, resourceVisitor.getResources()));
    }

//    @Override
//    public void visit(TypeDefinitionNode typeDefinitionNode) {
//        String entityName = typeDefinitionNode.typeName().text().trim();
//        String primaryKey = "";
//
//        List<Attribute> attributes = new ArrayList<>();
//        if (typeDefinitionNode.typeDescriptor() instanceof RecordTypeDescriptorNode) {
//            RecordTypeDescriptorNode recordTypeDescriptorNode = (RecordTypeDescriptorNode) typeDefinitionNode.typeDescriptor();
//            NodeList<Node> fields = recordTypeDescriptorNode.fields();
//            for (Node field : fields) {
//                if (field instanceof RecordFieldNode) {
//                    Attribute.Association association = null;
//                    String typeName = "";
//                    boolean isRequired = ((RecordFieldNode) field).questionMarkToken().isPresent();
//                    boolean isNillable = false;
//                    boolean isArray = false;
//                    RecordFieldNode recordFieldNode = (RecordFieldNode) field;
//                    Node typeNameNode = recordFieldNode.typeName();
//                    if (typeNameNode instanceof OptionalTypeDescriptorNode) {
//                        isNillable = true;
//                        Node typeDescriptor = ((OptionalTypeDescriptorNode) typeNameNode).typeDescriptor();
//                        if (typeDescriptor instanceof ArrayTypeDescriptorNode) {
//                            isArray = true;
//                            typeName = ((ArrayTypeDescriptorNode) typeDescriptor).memberTypeDesc().toString();
//                        } else {
//                            typeName = typeDescriptor.toString();
//                        }
//
//                    } else if (typeNameNode instanceof ArrayTypeDescriptorNode) {
//                        isArray = true;
//                        typeName = ((ArrayTypeDescriptorNode) typeNameNode).memberTypeDesc().toString();
//                    } else {
//                        typeName = recordFieldNode.typeName().toString();
//                    }
//
//                    String fieldName = recordFieldNode.fieldName().text().trim();
//
//                    if (!PRIMITIVE_TYPES.contains(typeName.trim()) && !(typeNameNode instanceof QualifiedNameReferenceNode)) {
//                        String selfCardinality = MultiServiceModelingConstants.CardinalityValue.ONE_AND_ONLY_ONE.getValue();
//                        String associateCardinality = MultiServiceModelingConstants.CardinalityValue.ONE.getValue();
//                        if ((isNillable || !isRequired) && !isArray) {
//                            associateCardinality = MultiServiceModelingConstants.CardinalityValue.ZERO_OR_ONE.getValue();
//                        } else if ((isNillable || !isRequired) && isArray) {
//                            associateCardinality = MultiServiceModelingConstants.CardinalityValue.ZERO_OR_MANY.getValue();
//                        } else if (isArray) {
//                            associateCardinality = MultiServiceModelingConstants.CardinalityValue.MANY.getValue();
//                        } else {
//                            associateCardinality = MultiServiceModelingConstants.CardinalityValue.ONE.getValue();
//                        }
//                        Attribute.Association.Cardinality cardinality = new Attribute.Association.Cardinality(selfCardinality, associateCardinality);
//                        //need to find the package of the associate entity
//                        association = new Attribute.Association(typeName.trim(), cardinality);
//
//                    }
//                    Attribute fieldAttribute = new Attribute(fieldName.trim(), typeName.trim(), isRequired, isNillable, "", association);
//                    attributes.add(fieldAttribute);
//                }
//
//            }
//
//            Entity entity = new Entity(attributes);
//            String packageInfo = String.join("/", this.packageId.getOrg(), this.packageId.getName(), this.packageId.getVersion());
//            this.entities.put(String.join("/", packageInfo, entityName), entity);
//        }
//    }

        @Override
        public void visit (ImportDeclarationNode importDeclarationNode){

        }

        @Override
        public void visit (EnumDeclarationNode enumDeclarationNode){

        }
    }

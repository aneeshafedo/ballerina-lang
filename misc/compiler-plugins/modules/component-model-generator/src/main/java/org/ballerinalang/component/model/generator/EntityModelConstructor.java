package org.ballerinalang.component.model.generator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.*;
import org.ballerinalang.component.model.generator.model.ComponentModel;
import org.ballerinalang.component.model.generator.model.entity.Attribute;
import org.ballerinalang.component.model.generator.model.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.component.model.generator.MultiServiceModelingConstants.COLON;
import static org.ballerinalang.component.model.generator.MultiServiceModelingConstants.FORWARD_SLASH;


public class EntityModelConstructor {
    public void constructEntityModel(SemanticModel semanticModel, Map<String, Entity> entities, ComponentModel.PackageId packageId) {
        List<Symbol> symbols = semanticModel.moduleSymbols();
        for (Symbol symbol : symbols) {
            if (symbol.kind().equals(SymbolKind.TYPE_DEFINITION)) {
                TypeDefinitionSymbol typeDefinitionSymbol = (TypeDefinitionSymbol) symbol;
                if (typeDefinitionSymbol.typeDescriptor() instanceof RecordTypeSymbol) {
                    String entityName = getEntityName(packageId, typeDefinitionSymbol.moduleQualifiedName());
                    List<Attribute> attributeList = new ArrayList<>();
                    RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeDefinitionSymbol.typeDescriptor();
                    Map<String, RecordFieldSymbol> recordFieldSymbolMap = recordTypeSymbol.fieldDescriptors();
                    for (Map.Entry<String, RecordFieldSymbol> fieldEntry : recordFieldSymbolMap.entrySet()) {

                        RecordFieldSymbol fieldEntryValue = fieldEntry.getValue();
                        String fieldName = fieldEntryValue.getName().get();
                        String fieldType = fieldEntryValue.typeDescriptor().signature();
                        boolean required = !fieldEntryValue.signature().endsWith("?"); //need to handle default
                        String defaultValue = ""; //need to address
                        boolean nillable = isNillable(fieldEntryValue.typeDescriptor());
                        List<Attribute.Association> associations = getAssociations(fieldEntryValue.typeDescriptor(), entityName, required, nillable);
                        Attribute attribute = new Attribute(fieldName, fieldType, required, nillable, defaultValue, associations);
                        attributeList.add(attribute);
                    }
                    Entity entity = new Entity(attributeList);
                    entities.put(entityName, entity);
                }
            }
        }
    }

    private boolean isNillable(TypeSymbol fieldTypeDescriptor) {
        boolean isNillable = false;
        if (fieldTypeDescriptor instanceof UnionTypeSymbol) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) fieldTypeDescriptor;
            List<TypeSymbol> memberTypeDescriptors = unionTypeSymbol.memberTypeDescriptors();
            isNillable = memberTypeDescriptors.stream().anyMatch(m -> m instanceof NilTypeSymbol);
        }
        return isNillable;
    }

    private String getSelfCardinality(TypeSymbol typeSymbol, String entityName) {
        String selfCardinality = MultiServiceModelingConstants.CardinalityValue.ONE_AND_ONLY_ONE.getValue();
        if (typeSymbol instanceof TypeReferenceTypeSymbol &&
                ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor() instanceof RecordTypeSymbol) {
            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) (((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor());
            Map<String, RecordFieldSymbol> recordFieldSymbolMap = recordTypeSymbol.fieldDescriptors();
            for (Map.Entry<String, RecordFieldSymbol> fieldEntry : recordFieldSymbolMap.entrySet()) {
                TypeSymbol fieldTypeDescriptor = fieldEntry.getValue().typeDescriptor();
                if (fieldTypeDescriptor instanceof TypeReferenceTypeSymbol) {
                    if (entityName.equals(fieldTypeDescriptor.signature())) {
                        selfCardinality = MultiServiceModelingConstants.CardinalityValue.ONE_AND_ONLY_ONE.getValue();
                    }
                } else if (fieldTypeDescriptor instanceof UnionTypeSymbol) {
                    boolean isFound = false;
                    boolean isNull = false;
                    UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) fieldTypeDescriptor;
                    List<TypeSymbol> memberTypeDescriptors = unionTypeSymbol.memberTypeDescriptors();
                    for (TypeSymbol memberTypeSymbol : memberTypeDescriptors) {
                        if (entityName.equals(memberTypeSymbol.signature())) {
                            isFound = true;
                        } else if (memberTypeSymbol instanceof NilTypeSymbol) {
                            isNull = true;
                        }
                    }
                    if (isFound && isNull) {
                        selfCardinality = MultiServiceModelingConstants.CardinalityValue.ZERO_OR_ONE.getValue();
                    }
                } else if (fieldTypeDescriptor instanceof ArrayTypeSymbol) {
                    if (fieldTypeDescriptor.signature().replace("[]", "").equals(entityName)) {
                        selfCardinality = MultiServiceModelingConstants.CardinalityValue.ZERO_OR_MANY.getValue();
                    }
                }
            }
        }
        return selfCardinality;
    }

    private String getAssociateCardinality(boolean isArray, boolean isRequired, boolean isNillable) {
        if (isArray && isRequired && !isNillable) {
            return MultiServiceModelingConstants.CardinalityValue.ONE_OR_MANY.getValue();
        } else if (isArray) {
            return MultiServiceModelingConstants.CardinalityValue.ZERO_OR_MANY.getValue();
        } else if (!isRequired || isNillable) {
            return MultiServiceModelingConstants.CardinalityValue.ZERO_OR_ONE.getValue();
        } else {
            return MultiServiceModelingConstants.CardinalityValue.ONE_AND_ONLY_ONE.getValue();
        }
    }

    private String getEntityName(ComponentModel.PackageId packageId, String moduleQualifiedName) {
        String entityName;
        String[] nameSpits = moduleQualifiedName.split(":");
        if (!nameSpits[0].equals(packageId.getName())) {
            entityName = packageId.getOrg() + FORWARD_SLASH + packageId.getName() + "." + nameSpits[0] + COLON +
                    packageId.getVersion() + COLON + nameSpits[1];
        } else {
            entityName = packageId.getOrg() + FORWARD_SLASH + nameSpits[0] + COLON + packageId.getVersion() +
                    COLON + nameSpits[1];
        }
        return entityName;
    }

    private List<Attribute.Association> getAssociationsInUnionTypes(UnionTypeSymbol unionTypeSymbol, String entityName, boolean isRequired) {
        List<Attribute.Association> unionTypeAssociations = new ArrayList<>();
        List<TypeSymbol> memberTypeDescriptors = unionTypeSymbol.memberTypeDescriptors();
        boolean isNullableAssociate = memberTypeDescriptors.stream().anyMatch(m -> m instanceof NilTypeSymbol);
        for (TypeSymbol typeSymbol : memberTypeDescriptors) {
            if (!(typeSymbol instanceof NilTypeSymbol)) {
                List<Attribute.Association> associations = getAssociations(typeSymbol, entityName, isRequired, isNullableAssociate);
                unionTypeAssociations.addAll(associations);
            }
        }
        return unionTypeAssociations;
    }
    private List<Attribute.Association> getAssociations(TypeSymbol fieldTypeDescriptor, String entityName, boolean isRequired, boolean isNillable) {
        List<Attribute.Association> associations = new ArrayList<>();
        if (fieldTypeDescriptor instanceof TypeReferenceTypeSymbol) {
            String associate = fieldTypeDescriptor.signature();
            Attribute.Association.Cardinality cardinality = new Attribute.Association.Cardinality(
                    getSelfCardinality(fieldTypeDescriptor, entityName),
                    getAssociateCardinality(false, isRequired, isNillable));
            associations.add(new Attribute.Association(associate, cardinality));
        } else if (fieldTypeDescriptor instanceof UnionTypeSymbol) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) fieldTypeDescriptor;
            associations.addAll(getAssociationsInUnionTypes(unionTypeSymbol, entityName, isRequired));
        } else if (fieldTypeDescriptor instanceof ArrayTypeSymbol) {
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) fieldTypeDescriptor;
            if (arrayTypeSymbol.memberTypeDescriptor() instanceof TypeReferenceTypeSymbol) {
                String associate = arrayTypeSymbol.signature().replace("[]", "");
                Attribute.Association.Cardinality cardinality = new Attribute.Association.Cardinality(
                        getSelfCardinality(arrayTypeSymbol, entityName),
                        getAssociateCardinality(true, isRequired, isNillable));
                associations.add(new Attribute.Association(associate, cardinality));
            }
        }
        return associations;
    }

    private String getValidAssociateEntityName(String originalName) {
        String validName = originalName;
        if (originalName.endsWith("?")) {
            validName = originalName.replace("?", "");
        } else {
            validName = originalName.replace("|()", "");
        }
        return validName;
    }
}

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

package io.ballerina.componentmodel.entitymodel;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.NilTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.componentmodel.ComponentModel;
import io.ballerina.componentmodel.ComponentModel.PackageId;
import io.ballerina.componentmodel.ComponentModelingConstants.CardinalityValue;
import io.ballerina.componentmodel.entitymodel.components.Association;
import io.ballerina.componentmodel.entitymodel.components.Attribute;
import io.ballerina.componentmodel.entitymodel.components.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.componentmodel.ComponentModelingConstants.ARRAY;
import static io.ballerina.componentmodel.ComponentModelingConstants.COLON;
import static io.ballerina.componentmodel.ComponentModelingConstants.FORWARD_SLASH;

/**
 * Build entity model to represent relationship between records.
 */
public class EntityModelGenerator {
    public void generateEntityModel(SemanticModel semanticModel, Map<String, Entity> entities,
                                    ComponentModel.PackageId packageId) {
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
                        // when is the field name is optional? Tested with record inclusion and res types
                        String fieldName = fieldEntryValue.getName().get(); // need to handle
                        String fieldType = fieldEntryValue.typeDescriptor().signature();
                        boolean required = !fieldEntryValue.isOptional();
                        String defaultValue = ""; //need to address
                        boolean nillable = isNillable(fieldEntryValue.typeDescriptor());
                        List<Association> associations =
                                getAssociations(fieldEntryValue.typeDescriptor(), entityName, required, nillable);
                        Attribute attribute =
                                new Attribute(fieldName, fieldType, required, nillable, defaultValue, associations);
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
        String selfCardinality = CardinalityValue.ONE_AND_ONLY_ONE.getValue();
        if (typeSymbol instanceof TypeReferenceTypeSymbol &&
                ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor() instanceof RecordTypeSymbol) {
            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol)
                    (((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor());
            Map<String, RecordFieldSymbol> recordFieldSymbolMap = recordTypeSymbol.fieldDescriptors();
            for (Map.Entry<String, RecordFieldSymbol> fieldEntry : recordFieldSymbolMap.entrySet()) {
                TypeSymbol fieldTypeDescriptor = fieldEntry.getValue().typeDescriptor();
                if (fieldTypeDescriptor instanceof TypeReferenceTypeSymbol) {
                    if (entityName.equals(fieldTypeDescriptor.signature())) {
                        selfCardinality = CardinalityValue.ONE_AND_ONLY_ONE.getValue();
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
                        selfCardinality = CardinalityValue.ZERO_OR_ONE.getValue();
                    }
                } else if (fieldTypeDescriptor instanceof ArrayTypeSymbol) {
                    if (fieldTypeDescriptor.signature().replace(ARRAY, "").equals(entityName)) {
                        selfCardinality = CardinalityValue.ZERO_OR_MANY.getValue();
                    }
                }
            }
        }
        return selfCardinality;
    }

    private String getAssociateCardinality(boolean isArray, boolean isRequired, boolean isNillable) {
        if (isArray && isRequired && !isNillable) {
            return CardinalityValue.ONE_OR_MANY.getValue();
        } else if (isArray) {
            return CardinalityValue.ZERO_OR_MANY.getValue();
        } else if (!isRequired || isNillable) {
            return CardinalityValue.ZERO_OR_ONE.getValue();
        } else {
            return CardinalityValue.ONE_AND_ONLY_ONE.getValue();
        }
    }

    /**
     * Build the FQN of the entity Ex: ballerina/reservation_api:0.1.0:Flight
     * @param packageId
     * @param moduleQualifiedName
     * @return
     */
    private String getEntityName(PackageId packageId, String moduleQualifiedName) {
        String entityName;
        String[] nameSpits = moduleQualifiedName.split(COLON);
        if (!nameSpits[0].equals(packageId.getName())) {
            entityName = packageId.getOrg() + FORWARD_SLASH + packageId.getName() + "." + nameSpits[0] + COLON +
                    packageId.getVersion() + COLON + nameSpits[1];
        } else {
            entityName = packageId.getOrg() + FORWARD_SLASH + nameSpits[0] + COLON + packageId.getVersion() +
                    COLON + nameSpits[1];
        }
        return entityName;
    }

    private List<Association> getAssociationsInUnionTypes(UnionTypeSymbol unionTypeSymbol, String entityName,
                                                          boolean isRequired) {
        List<Association> unionTypeAssociations = new ArrayList<>();
        List<TypeSymbol> memberTypeDescriptors = unionTypeSymbol.memberTypeDescriptors();
        boolean isNullableAssociate = memberTypeDescriptors.stream().anyMatch(m -> m instanceof NilTypeSymbol);
        for (TypeSymbol typeSymbol : memberTypeDescriptors) {
            if (!(typeSymbol instanceof NilTypeSymbol)) {
                List<Association> associations = getAssociations(typeSymbol, entityName,
                        isRequired, isNullableAssociate);
                unionTypeAssociations.addAll(associations);
            }
        }
        return unionTypeAssociations;
    }
    private List<Association> getAssociations(TypeSymbol fieldTypeDescriptor, String entityName, boolean isRequired,
                                              boolean isNillable) {
        List<Association> associations = new ArrayList<>();
        if (fieldTypeDescriptor instanceof TypeReferenceTypeSymbol) {
            String associate = fieldTypeDescriptor.signature();
            Association.Cardinality cardinality = new Association.Cardinality(
                    getSelfCardinality(fieldTypeDescriptor, entityName),
                    getAssociateCardinality(false, isRequired, isNillable));
            associations.add(new Association(associate, cardinality));
        } else if (fieldTypeDescriptor instanceof UnionTypeSymbol) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) fieldTypeDescriptor;
            associations.addAll(getAssociationsInUnionTypes(unionTypeSymbol, entityName, isRequired));
        } else if (fieldTypeDescriptor instanceof ArrayTypeSymbol) {
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) fieldTypeDescriptor;
            if (arrayTypeSymbol.memberTypeDescriptor() instanceof TypeReferenceTypeSymbol) {
                String associate = arrayTypeSymbol.signature().replace(ARRAY, "");
                Association.Cardinality cardinality = new Association.Cardinality(
                        getSelfCardinality(arrayTypeSymbol, entityName),
                        getAssociateCardinality(true, isRequired, isNillable));
                associations.add(new Association(associate, cardinality));
            }
        }
        return associations;
    }

//    private String getValidAssociateEntityName(String originalName) {
//        String validName = originalName;
//        if (originalName.endsWith("?")) {
//            validName = originalName.replace("?", "");
//        } else {
//            validName = originalName.replace("|()", "");
//        }
//        return validName;
//    }
}

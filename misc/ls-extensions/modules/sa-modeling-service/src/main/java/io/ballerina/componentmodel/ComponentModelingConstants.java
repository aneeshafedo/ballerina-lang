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

package io.ballerina.componentmodel;

/**
 * Constants use for Solution Architecture model generation.
 */
public class ComponentModelingConstants {

    /**
     * Enum to select the type of the parameter.
     */
    public enum ParameterIn  {
        BODY("body"),
        QUERY("query"),
        HEADER("header"),
        PATH("path");

        private final String parameterIn;

        ParameterIn(String parameterIn) {
            this.parameterIn = parameterIn;
        }

        public String getValue() {
            return this.parameterIn;
        }
    }

    public static final String CAPABILITY_NAME = "multiServiceModelingService";

    public static final String COLON = ":";
    public static final String FORWARD_SLASH = "/";
    public static final String SERVICE_ANNOTATION = "choreo:Service";
    public static final String CLIENT_ANNOTATION = "choreo:Client";
    public static final String ID = "id";
    public static final String SERVICE_ID = "serviceId";
    public static final String ARRAY = "[]";

    /**
     * Enum for cardinality types.
     */
    public enum CardinalityValue  {
        ZERO("0"),
        ZERO_OR_ONE("0-1"),
        ZERO_OR_MANY("0-m"),
        ONE("1"),
        ONE_AND_ONLY_ONE("1-1"),
        ONE_OR_MANY("1-m"),
        MANY("m");

        private final String cardinalityValue;

        CardinalityValue(String cardinalityValue) {
            this.cardinalityValue = cardinalityValue;
        }

        public String getValue() {
            return this.cardinalityValue;
        }
    }

}

package org.ballerinalang.component.model.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Constants use for Solution Architecture model generation.
 */
public class MultiServiceModelingConstants {

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

    public static final List<String> PRIMITIVE_TYPES = new ArrayList<>(
            Arrays.asList("string", "boolean", "int", "float", "decimal"));

    public static final String COLON = ":";
    public static final String FORWARD_SLASH = "/";

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

package io.ballerina.embeddedlang;

import org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;

public class EmbeddedLangSupportServerCapabilities extends BallerinaServerCapability {
    private boolean isSQLRegion;

    public EmbeddedLangSupportServerCapabilities() {
        super("embeddedLangSupportService");
    }


}

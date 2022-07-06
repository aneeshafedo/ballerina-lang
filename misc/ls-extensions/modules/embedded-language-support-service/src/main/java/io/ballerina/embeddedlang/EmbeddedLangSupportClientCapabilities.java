package io.ballerina.embeddedlang;

import org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;

public class EmbeddedLangSupportClientCapabilities extends BallerinaClientCapability {
    private boolean isSQLRegion;

    public boolean isSQLRegion() {
        return isSQLRegion;
    }

    public EmbeddedLangSupportClientCapabilities () {
        super("embeddedLangSupportService");
    }

    public void setSQLRegion(boolean SQLRegion) {
        isSQLRegion = SQLRegion;
    }

}

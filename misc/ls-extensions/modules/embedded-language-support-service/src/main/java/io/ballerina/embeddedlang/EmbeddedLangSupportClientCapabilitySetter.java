package io.ballerina.embeddedlang;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter;

@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter")
public class EmbeddedLangSupportClientCapabilitySetter extends BallerinaClientCapabilitySetter<EmbeddedLangSupportClientCapabilities> {

    @Override
    public String getCapabilityName() {
        return "embeddedLangSupportService";
    }

    @Override
    public Class<EmbeddedLangSupportClientCapabilities> getCapability() {
        return EmbeddedLangSupportClientCapabilities.class;
    }
}

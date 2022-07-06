package io.ballerina.embeddedlang.languages.sql;

public class SQLRegionIdentifierResponse {
    private boolean isSqlRegion;

    public SQLRegionIdentifierResponse(boolean isSqlRegion) {
        this.isSqlRegion = isSqlRegion;
    }

    public boolean isSqlRegion() {
        return isSqlRegion;
    }

    public void setSqlRegion(boolean sqlRegion) {
        isSqlRegion = sqlRegion;
    }

}

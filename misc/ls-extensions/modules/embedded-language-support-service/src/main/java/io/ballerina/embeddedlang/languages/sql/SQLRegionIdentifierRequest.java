package io.ballerina.embeddedlang.languages.sql;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;

public class SQLRegionIdentifierRequest {
    private TextDocumentIdentifier documentIdentifier;

    private Range lineRange;

    public TextDocumentIdentifier getDocumentIdentifier() {

        return documentIdentifier;
    }

    public void setDocumentIdentifier(TextDocumentIdentifier documentIdentifier) {

        this.documentIdentifier = documentIdentifier;
    }

    public Range getLineRange() {
        return lineRange;
    }

    public void setLineRange(Range lineRange) {
        this.lineRange = lineRange;
    }
}

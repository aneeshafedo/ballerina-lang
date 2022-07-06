package io.ballerina.embeddedlang;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.embeddedlang.languages.sql.SQLRegionIdentifierRequest;
import io.ballerina.embeddedlang.languages.sql.SQLRegionIdentifierResponse;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("embeddedLangSupportService")
public class EmbeddedLanguageSupportService implements ExtendedLanguageServerService {
    private WorkspaceManager workspaceManager;

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    public CompletableFuture<SQLRegionIdentifierResponse> isSQLRegion(SQLRegionIdentifierRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            SQLRegionIdentifierResponse sqlRegionIdentifierResponse = new SQLRegionIdentifierResponse(false);
            String fileUri = request.getDocumentIdentifier().getUri();
            Path path = Path.of(fileUri);
            Optional<SemanticModel> semanticModel = this.workspaceManager.semanticModel(path);

            Optional<Module> module = this.workspaceManager.module(path);
            if (semanticModel.isEmpty() || module.isEmpty()) {
                return sqlRegionIdentifierResponse;
            }

            Optional<Document> document = this.workspaceManager.document(path);
            if (document.isEmpty()) {
                return sqlRegionIdentifierResponse;
            }
            SyntaxTree syntaxTree = document.get().syntaxTree();
            NonTerminalNode currentNode = CommonUtil.findNode(request.getLineRange(), syntaxTree);
            if (currentNode instanceof TemplateExpressionNode) {
                sqlRegionIdentifierResponse.setSqlRegion(true);
            }
            return sqlRegionIdentifierResponse;
        });
    }
}

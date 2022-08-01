package org.ballerinalang.component.model.generator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import org.ballerinalang.component.model.generator.model.ComponentModel;
import org.ballerinalang.component.model.generator.model.Service;
import org.ballerinalang.component.model.generator.nodevisitors.ServiceNodeVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Construct component model fpr project with multiple service.
 */
public class ComponentModelConstructor {
    private ComponentModel componentModel;

    public ComponentModel getComponentModel() {
        return componentModel;
    }

    public void setComponentModel(ComponentModel componentModel) {
        this.componentModel = componentModel;
    }

    public void constructComponentModel(Project project, SemanticModel semanticModel) {
        List<Service> availableServices = new ArrayList<>();

        // get project from the workspace

        Package currentPackage = project.currentPackage();

        String packageName = String.valueOf(currentPackage.packageName());
        String packageOrg = String.valueOf(currentPackage.packageOrg());
        String packageVersion = String.valueOf(currentPackage.packageVersion());

        ComponentModel.PackageId packageId = new ComponentModel.PackageId(packageName, packageOrg, packageVersion);

        currentPackage.modules().forEach(module -> {
            Collection<DocumentId> documentIds = module.documentIds();
            for (DocumentId documentId : documentIds) {
                SyntaxTree syntaxTree = module.document(documentId).syntaxTree();
                ServiceNodeVisitor serviceNodeVisitor = new ServiceNodeVisitor(semanticModel,
                        module.document(documentId));
                syntaxTree.rootNode().accept(serviceNodeVisitor);
                availableServices.addAll(serviceNodeVisitor.getServices());
            }
            this.componentModel = new ComponentModel(packageId, availableServices);
        });
    }

}

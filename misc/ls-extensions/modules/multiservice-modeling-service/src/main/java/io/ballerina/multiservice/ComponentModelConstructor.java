package io.ballerina.multiservice;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.multiservice.model.ComponentModel;
import io.ballerina.multiservice.model.ComponentModel.PackageId;
import io.ballerina.multiservice.model.Service;
import io.ballerina.multiservice.model.entity.Entity;
import io.ballerina.multiservice.nodevisitors.ServiceNodeVisitor;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;

import java.util.*;

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

    public void constructComponentModel(Project project) {
        List<Service> availableServices = new ArrayList<>();
        Map<String, Entity> entities = new HashMap<>();

        // get project from the workspace

        Package currentPackage = project.currentPackage();


        String packageName = String.valueOf(currentPackage.packageName());
        String packageOrg = String.valueOf(currentPackage.packageOrg());
        String packageVersion = String.valueOf(currentPackage.packageVersion());

        PackageId packageId = new PackageId(packageName, packageOrg, packageVersion);

        currentPackage.modules().forEach(module -> {

            Collection<DocumentId> documentIds = module.documentIds();
            SemanticModel currentSemanticModel = currentPackage.getCompilation().getSemanticModel(module.moduleId());
            for (DocumentId documentId : documentIds) {

                SyntaxTree syntaxTree = module.document(documentId).syntaxTree();
                ServiceNodeVisitor serviceNodeVisitor = new ServiceNodeVisitor(currentSemanticModel,
                        module.document(documentId), packageId);
                syntaxTree.rootNode().accept(serviceNodeVisitor);
                availableServices.addAll(serviceNodeVisitor.getServices());
//                entities.putAll(serviceNodeVisitor.getEntities());
            }
            EntityModelConstructor entityModelConstructor = new EntityModelConstructor();
            entityModelConstructor.constructEntityModel(currentSemanticModel, entities, packageId);
            this.componentModel = new ComponentModel(packageId, availableServices, entities);
        });
    }




}

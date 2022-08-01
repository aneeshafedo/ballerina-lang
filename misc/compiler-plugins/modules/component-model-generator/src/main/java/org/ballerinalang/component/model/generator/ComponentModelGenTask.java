package org.ballerinalang.component.model.generator;

import com.google.gson.Gson;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.CompilationAnalysisContext;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.component.model.generator.model.ComponentModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class ComponentModelGenTask implements AnalysisTask<CompilationAnalysisContext> {

    static final String COMPONENT_MODEL_FILE = "component-model.json";

    @Override
    public void perform(CompilationAnalysisContext compilationAnalysisContext) {
        // when to write the file - when service has being defined ? or compilation option given
        // unable to find the semantic model
        Project project = compilationAnalysisContext.currentPackage().project();
        ComponentModelConstructor componentModelConstructor = new ComponentModelConstructor();
        componentModelConstructor.constructComponentModel(project, null);
        ComponentModel componentModel = componentModelConstructor.getComponentModel();
        Gson gson = new Gson();
        String componentModelStr = gson.toJson(componentModel);
        writeComponentModelJsonSchema(componentModelStr, project);
    }

    private void writeComponentModelJsonSchema(String schema, Project project) {
        Path path;
        if (project.kind().equals(ProjectKind.SINGLE_FILE_PROJECT)) {
            path = Paths.get(System.getProperty("user.dir"));
        } else {
            path = project.targetDir().resolve(ProjectConstants.BIN_DIR_NAME);
        }
        if (path != null && !schema.isEmpty()) {
            Path configSchemaFile = path.resolve(COMPONENT_MODEL_FILE);
            createIfNotExists(configSchemaFile);
            writeContent(configSchemaFile, schema);
        }

    }

    /**
     * Create the file in given path if not exists.
     *
     * @param filePath The path to write the config schema JSON
     */
    private static void createIfNotExists(Path filePath) {
        Path parentDir = filePath.getParent();
        if (parentDir != null && !parentDir.toFile().exists()) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException ioException) {
                throw new ProjectException("Failed to create " + parentDir.toString());
            }
        }
        if (!filePath.toFile().exists()) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new ProjectException("Failed to create " + filePath.toString());
            }
        }
    }

    /**
     * Write given content to the file.
     *
     * @param filePath Path to the file to write content
     * @param content  String content to write
     */
    private static void writeContent(Path filePath, String content) {
        try {
            Files.write(filePath, Collections.singleton(content));
        } catch (IOException e) {
            throw new ProjectException("Failed to write dependencies to the " + filePath.toString() + " file");
        }
    }
}

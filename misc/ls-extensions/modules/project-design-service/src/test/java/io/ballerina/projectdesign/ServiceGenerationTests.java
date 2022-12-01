package io.ballerina.projectdesign;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceGenerationTests {
    private static final Path RES_DIR = Paths.get("src", "test", "resources").toAbsolutePath();
    private static final String BALLERINA = "ballerina";
    private static final String RESULT = "result";
    private static final String MULTISERVICE_MODEL = "projectDesignService/getProjectComponentModels";

    @Test(description = "Test service model generation of multi module ballerina project")
    public void testMultiModuleProject() throws IOException, ExecutionException, InterruptedException {

        Path project = RES_DIR.resolve(BALLERINA).resolve(Path.of
                ("reservation_api", "reservation_service.bal").toString());

        Path expectedJsonPath = RES_DIR.resolve(RESULT).resolve(Path.of("reservation_service.json"));

        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, project);

        ProjectComponentRequest request = new ProjectComponentRequest();
        request.setDocumentUris(List.of(project.toString()));

        CompletableFuture<?> result = serviceEndpoint.request(MULTISERVICE_MODEL, request);
        ProjectComponentResponse response = (ProjectComponentResponse) result.get();
        Gson gson = new GsonBuilder().serializeNulls().create();

        JsonObject generatedJson = response.getComponentModels().get("test/reservation_api:0.1.0");
        ComponentModel generatedModel = gson.fromJson(generatedJson, ComponentModel.class);
        ComponentModel expectedModel = getComponentFromGivenJsonFile(expectedJsonPath);
        generatedModel.getServices().forEach((id, service) -> {
            String generatedService = gson.toJson(service).replaceAll("\\s+", "");
            String expectedService = gson.toJson(expectedModel.getServices().get(id)).
                    replaceAll("\\s+", "");
            Assert.assertEquals(generatedService, expectedService);
        });
    }

    @Test(description = "Test service model generation of multipackage environment")
    public void testGRPCServiceGenerationForWorkspace() throws IOException, ExecutionException, InterruptedException {

        Path project1 = RES_DIR.resolve(BALLERINA).resolve(
                Path.of("microservice_grpc/cart", "cart_service.bal").toString());
        Path project2 = RES_DIR.resolve(BALLERINA).resolve(
                Path.of("microservice_grpc/checkout", "checkout_service.bal").toString());
        Path project3 = RES_DIR.resolve(BALLERINA).resolve(
                Path.of("microservice_grpc/frontend", "service.bal").toString());

        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();

        ProjectComponentRequest request = new ProjectComponentRequest();
        request.setDocumentUris(List.of(project1.toString(), project2.toString(), project3.toString()));

        CompletableFuture<?> result = serviceEndpoint.request(MULTISERVICE_MODEL, request);
        ProjectComponentResponse response = (ProjectComponentResponse) result.get();

        Gson gson = new GsonBuilder().serializeNulls().create();
        response.getComponentModels().forEach((id, model) -> {
            ComponentModel generatedModel = gson.fromJson(model, ComponentModel.class);
            String sampleJsonFile =  String.format("%s.json",generatedModel.getPackageId().getName());
            try {
                ComponentModel expectedModel = getComponentFromGivenJsonFile(
                        RES_DIR.resolve(RESULT).resolve(Path.of(sampleJsonFile)));
                generatedModel.getServices().forEach((s, service) -> {
                    String generatedService = gson.toJson(service).replaceAll("\\s+", "");
                    String expectedService = gson.toJson(expectedModel.getServices().get(id)).
                            replaceAll("\\s+", "");
                    Assert.assertEquals(generatedService, expectedService);
                });
            } catch (IOException e) {
                Assert.fail("Unable to locate the given resource paths.");
            }
        });
    }

    public static ComponentModel getComponentFromGivenJsonFile(Path expectedFilePath) throws IOException {
        Stream<String> lines = Files.lines(expectedFilePath);
        String content = lines.collect(Collectors.joining(System.lineSeparator()));
        lines.close();
        Gson gson = new GsonBuilder().serializeNulls().create();

        ComponentModel componentModel = gson.fromJson(content, ComponentModel.class);
        return componentModel;
    }
}

package io.github.hejcz;

import io.micronaut.context.ApplicationContext;
import io.micronaut.http.client.HttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Inject;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.testcontainers.containers.MongoDBContainer;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;

public class MongoFriendlyMicronautTest implements BeforeEachCallback, AfterEachCallback {

    MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.9")
            .withExposedPorts(27017);

    private ApplicationContext applicationContext;

    private EmbeddedServer embeddedServer;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        mongoDBContainer.start();

        applicationContext = ApplicationContext.run(
                Map.of("mongodb.uri", "mongodb://localhost:" + mongoDBContainer.getMappedPort(27017)),
                "test");

        for (Object instance : context.getRequiredTestInstances().getAllInstances()) {
            for (Field field : instance.getClass().getDeclaredFields()) {
                if (field.getDeclaredAnnotation(Mock.class) != null) {
                    applicationContext.registerSingleton(field.get(instance));
                }
            }
        }

        embeddedServer = applicationContext.getBean(EmbeddedServer.class);
        applicationContext.start();
        embeddedServer.start();

        for (Object instance : context.getRequiredTestInstances().getAllInstances()) {
            for (Field field : instance.getClass().getDeclaredFields()) {
                if (field.getDeclaredAnnotation(Inject.class) != null) {
                    if (field.getType().equals(HttpClient.class)) {
                        field.set(instance, HttpClient.create(new URL("http", "localhost", embeddedServer.getPort(), "")));
                    }
                }
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        applicationContext.stop();
        embeddedServer.stop();
        if (mongoDBContainer.isRunning()) {
            mongoDBContainer.stop();
        }
    }
}

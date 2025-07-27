package nl.mineburg.grafiq.annotation.processor;

import nl.mineburg.grafiq.GrafiqClient;
import nl.mineburg.grafiq.annotation.GrafiqAnalytic;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.Set;

/**
 * Processor for discovering and registering analytic classes annotated with
 * {@link nl.mineburg.grafiq.annotation.GrafiqAnalytic}.
 * <p>
 * Uses the Reflections library to scan for annotated classes and registers them
 * with the {@link nl.mineburg.grafiq.registry.GrafiqTableRegistry}.
 */
public class GrafiqAnalyticProcessor {

    private final GrafiqClient client;

    private GrafiqAnalyticProcessor(GrafiqClient client) {
        this.client = client;
    }

    /**
     * Creates a new {@code GrafiqAnalyticProcessor} for the given client.
     *
     * @param client the {@link GrafiqClient} instance to use
     * @return a new {@code GrafiqAnalyticProcessor} instance
     */
    public static GrafiqAnalyticProcessor of(GrafiqClient client) {
        return new GrafiqAnalyticProcessor(client);
    }

    /**
     * Processes all classes annotated with {@link nl.mineburg.grafiq.annotation.GrafiqAnalytic}
     * in the specified package path. Registers each found analytic class and its table
     * in the {@link nl.mineburg.grafiq.registry.GrafiqTableRegistry}.
     *
     * @param path the package path to scan for analytic classes (e.g., "nl.mineburg.grafiq.test")
     */
    public void process(String path) {
        Reflections reflections = new Reflections(path, Scanners.TypesAnnotated);

        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(GrafiqAnalytic.class);
        annotatedClasses.forEach(clazz -> {
            GrafiqAnalytic annotation = clazz.getAnnotation(GrafiqAnalytic.class);
            client.tableRegistry().register(clazz, annotation.table());
        });
    }

}

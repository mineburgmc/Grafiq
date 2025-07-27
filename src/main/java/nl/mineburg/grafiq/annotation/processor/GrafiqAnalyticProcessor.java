package nl.mineburg.grafiq.annotation.processor;

import nl.mineburg.grafiq.GrafiqClient;
import nl.mineburg.grafiq.annotation.GrafiqAnalytic;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.Set;

public class GrafiqAnalyticProcessor {

    private final GrafiqClient client;

    private GrafiqAnalyticProcessor(GrafiqClient client) {
        this.client = client;
    }

    public static GrafiqAnalyticProcessor of(GrafiqClient client) {
        return new GrafiqAnalyticProcessor(client);
    }

    public void process(String path) {
        Reflections reflections = new Reflections(path, Scanners.TypesAnnotated);

        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(GrafiqAnalytic.class);
        annotatedClasses.forEach(clazz -> {
            GrafiqAnalytic annotation = clazz.getAnnotation(GrafiqAnalytic.class);
            client.tableRegistry().register(clazz, annotation.table());
        });
    }

}

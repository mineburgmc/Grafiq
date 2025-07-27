package nl.mineburg.grafiq;

import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.insert.InsertSettings;
import lombok.Getter;
import nl.mineburg.grafiq.annotation.GrafiqAnalytic;
import nl.mineburg.grafiq.annotation.processor.GrafiqAnalyticProcessor;
import nl.mineburg.grafiq.interfaces.GrafiqInterface;
import nl.mineburg.grafiq.registry.GrafiqTableRegistry;
import nl.mineburg.grafiq.registry.GrafiqTypeRegistry;
import nl.mineburg.grafiq.utils.RecordMatchingStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main client for interacting with ClickHouse via Grafiq.
 * <p>
 * Handles type and table registration, analytic processing, and event tracking.
 */
@Getter
public class GrafiqClient {

    @Getter // This only really exists because I want the interface to also support tracking, lazy me :DD
    private static GrafiqClient instance;

    private final Client clickhouseClient;
    private final GrafiqTypeRegistry typeRegistry;
    private final GrafiqTableRegistry tableRegistry;
    private final GrafiqAnalyticProcessor analyticProcessor;

    private GrafiqClient(Client.Builder builder) {
        this.clickhouseClient = builder
                .columnToMethodMatchingStrategy(RecordMatchingStrategy.INSTANCE)
                .build();
        this.typeRegistry = GrafiqTypeRegistry.create();
        this.tableRegistry = GrafiqTableRegistry.of(this);
        this.analyticProcessor = GrafiqAnalyticProcessor.of(this);

        instance = this;
    }

    /**
     * Creates a new {@code GrafiqClient} instance using the given ClickHouse client builder.
     *
     * @param builder the ClickHouse {@link Client.Builder} to use
     * @return a new {@code GrafiqClient} instance
     */
    public static GrafiqClient of(Client.Builder builder) {
        return new GrafiqClient(builder);
    }

    /**
     * Tracks one or more analytic events.
     * <p>
     * Accepts a variable number of events and delegates to {@link #track(List)}.
     *
     * @param events the analytic events to track
     * @param <T>    the type of analytic event
     */
    @SafeVarargs
    public final <T> void track(T... events) {
        this.track(List.of(events));
    }

    /**
     * Tracks a list of analytic events.
     * <p>
     * Groups events by class, validates annotations and interfaces, and inserts them into ClickHouse.
     *
     * @param events the list of analytic events to track
     * @param <T>    the type of analytic event
     * @throws IllegalArgumentException if an event class is missing required annotations or interfaces
     */
    public final <T> void track(List<T> events) {
        if (events.isEmpty()) return;

        Map<Class<?>, List<T>> grouped = new HashMap<>();
        for (T event : events) {
            grouped.computeIfAbsent(event.getClass(), k -> new ArrayList<>()).add(event);
        }

        for (Map.Entry<Class<?>, List<T>> entry : grouped.entrySet()) {
            Class<?> clazz = entry.getKey();
            if (!clazz.isAnnotationPresent(GrafiqAnalytic.class)) {
                throw new IllegalArgumentException("Missing @GrafiqAnalytic on: " + clazz.getName());
            }

            if (!GrafiqInterface.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Missing GrafiqInterface on: " + clazz.getName() + ", " +
                        "did you forget to implement it?");
            }

            String table = tableRegistry.table(clazz);
            clickhouseClient.insert(table, entry.getValue(), new InsertSettings());
        }
    }

    /**
     * Processes and registers all analytic classes in the specified package path.
     *
     * @param path the package path to scan (e.g., "nl.mineburg.grafiq.test")
     */
    public void process(String path) {
        analyticProcessor.process(path);
    }

    /**
     * Shuts down the ClickHouse client and releases resources.
     */
    public void shutdown() {
        clickhouseClient.close();
    }

}
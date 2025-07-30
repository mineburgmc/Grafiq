package nl.mineburg.grafiq;

import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.insert.InsertSettings;
import lombok.Getter;
import lombok.Setter;
import nl.mineburg.grafiq.annotation.GrafiqAnalytic;
import nl.mineburg.grafiq.annotation.processor.GrafiqAnalyticProcessor;
import nl.mineburg.grafiq.interfaces.GrafiqInterface;
import nl.mineburg.grafiq.processing.GrafiqProcessor;
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

    private final GrafiqProcessor processor;
    private final GrafiqTypeRegistry typeRegistry;
    private final GrafiqTableRegistry tableRegistry;
    private final GrafiqAnalyticProcessor analyticProcessor;

    private GrafiqClient(Client.Builder builder) {
        this.processor = GrafiqProcessor.of(this, () -> builder
                .columnToMethodMatchingStrategy(RecordMatchingStrategy.INSTANCE)
                .build());
        this.typeRegistry = GrafiqTypeRegistry.create();
        this.tableRegistry = GrafiqTableRegistry.of(this);
        this.analyticProcessor = GrafiqAnalyticProcessor.of(this);

        processor.process();
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
     */
    @SafeVarargs
    public final <T extends Record> void track(T... events) {
        this.track(List.of(events));
    }

    /**
     * Tracks a list of analytic events.
     * <p>
     * Groups events by class, validates annotations and interfaces, and inserts them into ClickHouse.
     *
     * @param events the list of analytic events to track
     * @throws IllegalArgumentException if an event class is missing required annotations or interfaces
     */
    public final void track(List<? extends Record> events) {
        if (events.isEmpty()) return;
        processor.queue().addAll(events);
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
        processor.shutdown();
    }

}
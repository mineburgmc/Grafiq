package nl.mineburg.grafiq.processing;

import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.insert.InsertSettings;
import com.clickhouse.client.api.metadata.TableSchema;
import lombok.Getter;
import nl.mineburg.grafiq.GrafiqClient;
import nl.mineburg.grafiq.annotation.GrafiqAnalytic;
import nl.mineburg.grafiq.interfaces.GrafiqInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class GrafiqProcessor {

    private GrafiqClient client;
    private Client clickhouseClient;
    private final ScheduledExecutorService executor;
    private final Supplier<Client> clientSupplier;

    @Getter
    private final ConcurrentLinkedQueue<Record> queue;
    private final ConcurrentLinkedQueue<Runnable> tableQueue;

    private GrafiqProcessor(GrafiqClient client, Supplier<Client> clientSupplier) {
        this.client = client;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.clientSupplier = clientSupplier;

        this.queue = new ConcurrentLinkedQueue<>();
        this.tableQueue = new ConcurrentLinkedQueue<>();
    }

    public static GrafiqProcessor of(GrafiqClient client, Supplier<Client> clientSupplier) {
        return new GrafiqProcessor(client, clientSupplier);
    }

    public void process() {
        executor.submit(() -> {
            // Clickhouse is a bit unpredictable, so I'll keep it contained for now because uh IDK
            clickhouseClient = clientSupplier.get();
            if (clickhouseClient == null) {
                throw new IllegalStateException("Failed to create ClickHouse client");
            }

            // First process the tables ;D
            Runnable task;
            while ((task = tableQueue.poll()) != null) {
                task.run();
            }

            executor.scheduleAtFixedRate(() -> {
                List<Record> drainedEvents = new ArrayList<>();
                Record event;
                while ((event = queue.poll()) != null) {
                    drainedEvents.add(event);
                }

                if (drainedEvents.isEmpty()) {
                    return;
                }

                Map<Class<?>, List<Record>> grouped = new HashMap<>();
                for (Record e : drainedEvents) {
                    grouped.computeIfAbsent(e.getClass(), k -> new ArrayList<>()).add(e);
                }

                for (Map.Entry<Class<?>, List<Record>> entry : grouped.entrySet()) {
                    Class<?> clazz = entry.getKey();

                    if (!clazz.isAnnotationPresent(GrafiqAnalytic.class)) {
                        throw new IllegalArgumentException("Missing @GrafiqAnalytic on: " + clazz.getName());
                    }

                    if (!GrafiqInterface.class.isAssignableFrom(clazz)) {
                        throw new IllegalArgumentException("Missing GrafiqInterface on: " + clazz.getName() +
                                ", did you forget to implement it?");
                    }

                    String table = client.tableRegistry().table(clazz);
                    clickhouseClient.insert(table, entry.getValue(), new InsertSettings());
                }
            }, 0, 50, TimeUnit.MILLISECONDS); // TODO make this editable
        });
    }

    public void createTable(String query, String table, Class<?> clazz) {
        tableQueue.offer(() -> {
            clickhouseClient.execute(query).thenAccept((action) -> {
                TableSchema schema = clickhouseClient.getTableSchema(table);
                clickhouseClient.register(clazz, schema);
            });
        });
    }

    public void shutdown() {
        executor.submit(() -> {
            if (clickhouseClient != null) {
                clickhouseClient.close();
            }
        });

        executor.shutdown();
    }

}
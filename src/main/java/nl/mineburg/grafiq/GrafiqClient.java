package nl.mineburg.grafiq;

import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.insert.InsertSettings;
import com.clickhouse.client.api.metadata.DefaultColumnToMethodMatchingStrategy;
import lombok.Getter;
import nl.mineburg.grafiq.annotation.GrafiqAnalytic;
import nl.mineburg.grafiq.annotation.processor.GrafiqAnalyticProcessor;
import nl.mineburg.grafiq.registry.GrafiqTableRegistry;
import nl.mineburg.grafiq.registry.GrafiqTypeRegistry;

import java.util.List;


@Getter
public class GrafiqClient {

    private final Client clickhouseClient;
    private final GrafiqTypeRegistry typeRegistry;
    private final GrafiqTableRegistry tableRegistry;
    private final GrafiqAnalyticProcessor analyticProcessor;

    private GrafiqClient(Client clickhouseClient) {
        this.clickhouseClient = clickhouseClient;
        this.typeRegistry = GrafiqTypeRegistry.create();
        this.tableRegistry = GrafiqTableRegistry.of(this);
        this.analyticProcessor = GrafiqAnalyticProcessor.of(this);
    }

    public static GrafiqClient of(Client client) {
        return new GrafiqClient(client);
    }

    public <T extends Record> void track(T event) {
        Class<?> eventClass = event.getClass();
        if (!eventClass.isAnnotationPresent(GrafiqAnalytic.class)) {
            throw new IllegalArgumentException("Analytic class is not annotated with @GrafiqAnalytic");
        }

        String table = tableRegistry.table(eventClass);
        clickhouseClient.insert(table, List.of(event), new InsertSettings());
    }

    public void process(String path) {
        analyticProcessor.process(path);
    }

    public void shutdown() {
        clickhouseClient.close();
    }

}

// TODO add a thread pool for querying instead of doing it instant like this
// TODO add batch processing support
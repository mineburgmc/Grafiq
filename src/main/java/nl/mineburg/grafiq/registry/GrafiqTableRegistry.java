package nl.mineburg.grafiq.registry;

import com.clickhouse.client.api.data_formats.RowBinaryFormatSerializer;
import com.clickhouse.client.api.data_formats.internal.SerializerUtils;
import com.clickhouse.client.api.metadata.TableSchema;
import com.clickhouse.data.ClickHouseColumn;
import nl.mineburg.grafiq.GrafiqClient;
import nl.mineburg.grafiq.annotation.GrafiqAnalytic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrafiqTableRegistry {

    private final GrafiqClient client;
    private final Map<Class<?>, String> tableRegistry = new HashMap<>();

    private GrafiqTableRegistry(GrafiqClient client) {
        this.client = client;
    }

    public static GrafiqTableRegistry of(GrafiqClient client) {
        return new GrafiqTableRegistry(client);
    }


    public void register(Class<?> clazz, String table) {
        GrafiqAnalytic annotation = clazz.getAnnotation(GrafiqAnalytic.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Analytic class is not annotated with @GrafiqAnalytic");
        }

        tableRegistry.put(clazz, table);
        this.createTable(clazz);
    }

    private void createTable(Class<?> clazz) {
        String table = table(clazz);
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS ").append(table).append(" (");

        List<String> definitions = new ArrayList<>();
        for (var component : clazz.getRecordComponents()) {
            String name = component.getName();
            Class<?> type = component.getType();

            var mapping = client.typeRegistry().mapping(type);
            definitions.add(name + " " + mapping.type());
        }

        definitions.add("created_at DateTime DEFAULT now()");
        query.append(String.join(", ", definitions));

        query.append(") ENGINE = MergeTree() ORDER BY created_at");

        client.clickhouseClient().execute(query.toString()).thenAccept((action) -> {
            TableSchema schema = client.clickhouseClient().getTableSchema(table);
            client.clickhouseClient().register(clazz, schema);
        });
    }

    public String table(Class<?> clazz) {
        return tableRegistry.get(clazz);
    }

}

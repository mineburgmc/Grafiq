package nl.mineburg.grafiq.registry;

import com.clickhouse.client.api.metadata.TableSchema;
import nl.mineburg.grafiq.GrafiqClient;
import nl.mineburg.grafiq.annotation.GrafiqAnalytic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for mapping analytic classes to their corresponding ClickHouse table names.
 * <p>
 * Handles registration of analytic classes, table creation, and table name lookups.
 */
public class GrafiqTableRegistry {

    private final GrafiqClient client;
    private final Map<Class<?>, String> tableRegistry = new HashMap<>();

    private GrafiqTableRegistry(GrafiqClient client) {
        this.client = client;
    }

    /**
     * Creates a new {@code GrafiqTableRegistry} for the given client.
     *
     * @param client the {@link GrafiqClient} instance to use
     * @return a new {@code GrafiqTableRegistry} instance
     */
    public static GrafiqTableRegistry of(GrafiqClient client) {
        return new GrafiqTableRegistry(client);
    }

    /**
     * Registers an analytic class and its associated table name.
     * <p>
     * Also triggers table creation in ClickHouse if it does not exist.
     *
     * @param clazz the analytic class to register
     * @param table the table name to associate with the class
     * @throws IllegalArgumentException if the class is not annotated with {@link GrafiqAnalytic}
     */
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
        client.processor().createTable(query.toString(), table, clazz);
    }

    /**
     * Returns the table name associated with the specified analytic class.
     *
     * @param clazz the analytic class
     * @return the table name, or {@code null} if not registered
     */
    public String table(Class<?> clazz) {
        return tableRegistry.get(clazz);
    }

}

package nl.mineburg.grafiq.registry;

import com.clickhouse.data.ClickHouseValue;

import java.util.function.Function;

public record GrafiqTypeMapping<T>(String type, Function<T, Object> converter) {
    public static GrafiqTypeMapping<?> of(String type, Function<?, Object> converter) {
        return new GrafiqTypeMapping<>(type, converter);
    }
}

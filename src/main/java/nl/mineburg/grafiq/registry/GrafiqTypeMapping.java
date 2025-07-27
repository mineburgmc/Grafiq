package nl.mineburg.grafiq.registry;

import java.util.function.Function;

/**
 * Represents a mapping between a Java type and its corresponding ClickHouse type,
 * including a converter function for value transformation.
 * <p>
 * The converter is currently unused, as ClickHouse does not support custom serialization.
 * Although, it's on the roadmap, so it's good practice to make the converter anyway! :D
 *
 * @param <T> the Java type this mapping applies to
 */
public record GrafiqTypeMapping<T>(String type, Function<Object, T> converter) {

    /**
     * Creates a new {@code GrafiqTypeMapping} with the specified ClickHouse type and converter.
     *
     * @param type      the ClickHouse type as a {@link String}
     * @param converter a {@link Function} to convert Java objects to the mapped type
     * @return a new {@code GrafiqTypeMapping} instance
     */
    public static GrafiqTypeMapping<?> of(String type, Function<Object, ?> converter) {
        return new GrafiqTypeMapping<>(type, converter);
    }
}

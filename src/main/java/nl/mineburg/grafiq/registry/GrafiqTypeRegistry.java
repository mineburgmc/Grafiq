package nl.mineburg.grafiq.registry;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Registry for mapping Java types to their corresponding ClickHouse type mappings.
 * <p>
 * Provides default mappings for common Java types and allows custom mappings to be registered.
 * Be aware, the converter is currently NOT used, it's purely because it's on our roadmap.
 */
@Getter
public class GrafiqTypeRegistry {


    private final Map<Class<?>, GrafiqTypeMapping> typeMappings = new HashMap<>();

    private GrafiqTypeRegistry() {
        this.defaultMappings();
    }

    /**
     * Creates a new {@code GrafiqTypeRegistry} instance with default type mappings.
     *
     * @return a new {@code GrafiqTypeRegistry} instance
     */
    public static GrafiqTypeRegistry create() {
        return new GrafiqTypeRegistry();
    }

    private void defaultMappings() {
        register(String.class, GrafiqTypeMapping.of("String", s -> s));

        register(Integer.class, GrafiqTypeMapping.of("Int32", i -> i));
        register(int.class, GrafiqTypeMapping.of("Int32", i -> i));

        register(Long.class, GrafiqTypeMapping.of("Int64", l -> l));
        register(long.class, GrafiqTypeMapping.of("Int64", l -> l));

        register(Short.class, GrafiqTypeMapping.of("Int16", s -> s));
        register(short.class, GrafiqTypeMapping.of("Int16", s -> s));

        register(Byte.class, GrafiqTypeMapping.of("Int8", b -> b));
        register(byte.class, GrafiqTypeMapping.of("Int8", b -> b));

        register(Float.class, GrafiqTypeMapping.of("Float32", f -> f));
        register(float.class, GrafiqTypeMapping.of("Float32", f -> f));

        register(Double.class, GrafiqTypeMapping.of("Float64", d -> d));
        register(double.class, GrafiqTypeMapping.of("Float64", d -> d));

        register(BigDecimal.class, GrafiqTypeMapping.of("Decimal", d -> d));

        register(Boolean.class, GrafiqTypeMapping.of("Bool", b -> b));
        register(boolean.class, GrafiqTypeMapping.of("Bool", b -> b));

        register(UUID.class, GrafiqTypeMapping.of("String", Object::toString));
    }

    /**
     * Registers a custom type mapping for the specified Java class.
     *
     * @param clazz   the Java class to map
     * @param mapping the {@link GrafiqTypeMapping} to associate with the class
     * @param <T>     the type of the Java class
     */
    public <T> void register(Class<T> clazz, GrafiqTypeMapping<?> mapping) {
        typeMappings.put(clazz, mapping);
    }

    /**
     * Retrieves the {@link GrafiqTypeMapping} associated with the specified Java class.
     *
     * @param clazz the Java class to look up
     * @return the associated {@link GrafiqTypeMapping}, or {@code null} if not registered
     */
    public GrafiqTypeMapping<?> mapping(Class<?> clazz) {
        return typeMappings.get(clazz);
    }

}

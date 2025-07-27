package nl.mineburg.grafiq.registry;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
public class GrafiqTypeRegistry {

    private final Map<Class<?>, GrafiqTypeMapping<?>> typeMappings = new HashMap<>();

    public GrafiqTypeRegistry() {
        this.defaultMappings();
    }

    public static GrafiqTypeRegistry create() {
        return new GrafiqTypeRegistry();
    }

    public void defaultMappings() {
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
    }

    public <T> void register(Class<T> clazz, GrafiqTypeMapping<?> mapping) {
        typeMappings.put(clazz, mapping);
    }

    public GrafiqTypeMapping<?> mapping(Class<?> clazz) {
        return typeMappings.get(clazz);
    }

}

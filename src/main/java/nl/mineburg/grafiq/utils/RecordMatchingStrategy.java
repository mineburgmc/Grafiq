package nl.mineburg.grafiq.utils;

import com.clickhouse.client.api.metadata.DefaultColumnToMethodMatchingStrategy;

/**
 * No documentation for this class, please do not use this anywhere else, it's purely for internal purposes :D
 */
public class RecordMatchingStrategy extends DefaultColumnToMethodMatchingStrategy {

    public static final RecordMatchingStrategy INSTANCE = new RecordMatchingStrategy();

    private RecordMatchingStrategy() {
        super();
    }

    @Override
    public boolean isGetter(String methodName) {
        return !isSetter(methodName);
    }

}
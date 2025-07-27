package nl.mineburg.grafiq.utils;

import com.clickhouse.client.api.metadata.DefaultColumnToMethodMatchingStrategy;

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
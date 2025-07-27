package nl.mineburg.grafiq.interfaces;

public interface GrafiqInterface {

    // Do not ask me why, this is just a ClickHouse serializer moment
    default String createdAt() {
        return null;
    }

}

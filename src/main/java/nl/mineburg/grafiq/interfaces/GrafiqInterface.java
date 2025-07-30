package nl.mineburg.grafiq.interfaces;

import nl.mineburg.grafiq.GrafiqClient;

/**
 * Interface to be implemented by analytic records for Grafiq.
 * <p>
 * Made so that the Clickhouse serializer works, and for some QoL stuff.
 */
public interface GrafiqInterface {

    /**
     * Returns the creation timestamp for the analytic record.
     * <p>
     * By default, returns {@code null}. Do not ask me why, this is just a ClickHouse serializer moment.
     * Please do not override this, as it does absolutely nothing.
     *
     * @return the creation timestamp as a {@link String}, or {@code null} if not set
     */
    default String createdAt() {
        return null;
    }

    /**
     * Tracks this analytic event using the current {@link GrafiqClient} instance.
     * <p>
     * If no client instance is available, the method does nothing.
     */
    default void track() {
        GrafiqClient client = GrafiqClient.instance();
        if (client == null) return; // Too bored to handle errors, just make the instance goddammit.
        if (!(this instanceof Record)) return;

        client.track((Record) this);
    }

}

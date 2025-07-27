import com.clickhouse.client.api.Client;
import lombok.SneakyThrows;
import nl.mineburg.grafiq.GrafiqClient;
import nl.mineburg.grafiq.annotation.GrafiqAnalytic;
import nl.mineburg.grafiq.interfaces.GrafiqInterface;
import nl.mineburg.grafiq.utils.RecordMatchingStrategy;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class GrafiqTest {

    @SneakyThrows
    @Test
    public void grafiqTest() {
        GrafiqClient client = GrafiqClient.of(new Client.Builder()
                .addEndpoint("http://148.251.23.114:18123/")
                .setUsername("default")
                .setPassword("penis")
                .setConnectTimeout(5000)
                .columnToMethodMatchingStrategy(RecordMatchingStrategy.INSTANCE)
                .build());
        client.tableRegistry().register(TestAnalytic.class, "test_table_new_new_test");
        client.tableRegistry().register(PlayerDropAnalytic.class, "player_drop_table_new_new_test");

        client.track(TestAnalytic.of("coole data"));
        client.track(TestAnalytic.of("niga"));
        client.track(PlayerDropAnalytic.of(UUID.randomUUID().toString(), "minecraft:stone", 10));
        client.track(PlayerDropAnalytic.of(UUID.randomUUID().toString(), "minecraft:diamond_ore", 8));

        client.shutdown();
    }


    @GrafiqAnalytic(table = "test_table_new_new_test")
    public record TestAnalytic(String name) implements GrafiqInterface {
        public static TestAnalytic of(String name) {
            return new TestAnalytic(name);
        }
    }

    @GrafiqAnalytic(table = "player_drop_table_new_new_test")
    public record PlayerDropAnalytic(String uuid, String item, int amount) implements GrafiqInterface {
        public static PlayerDropAnalytic of(String uuid, String item, int amount) {
            return new PlayerDropAnalytic(uuid, item, amount);
        }
    }

}

package nl.mineburg.grafiq.test;

import com.clickhouse.client.api.Client;
import lombok.SneakyThrows;
import nl.mineburg.grafiq.GrafiqClient;
import nl.mineburg.grafiq.annotation.GrafiqAnalytic;
import nl.mineburg.grafiq.interfaces.GrafiqInterface;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class GrafiqTest {

    @SneakyThrows
    @Test
    public void grafiqTest() {
        GrafiqClient client = GrafiqClient.of(new Client.Builder()
                .addEndpoint("https://localhost:8443") // Arbitrary login data, please fill in your own if you're running these tests. <3
                .setUsername("default")
                .setPassword("default")
                .setConnectTimeout(5000));
        client.process("nl.mineburg.grafiq.test");

        // Batch processing example! Yes, it supports different analytics at the same time (if you really want to)
        client.track(
                TestAnalytic.of(UUID.randomUUID(), "Such a sick message!"),
                TestAnalytic.of(UUID.randomUUID(), "Mineburg is so cool."),
                TestAnalytic.of(UUID.randomUUID(), "Willem & Sven made this, sicko mode.")
        );

        // You can also use lists!
        List<TestAnalytic> analytics = IntStream.range(0, 3).mapToObj(i -> TestAnalytic.of(UUID.randomUUID(),
                        "Cool message, number " + i)).toList();
        client.track(analytics);

        // Single processing is also supported.
        client.track(TestAnalytic.of(UUID.randomUUID(), "Yay!"));

        // Easiest processing example, be aware that the instance has to be made before this works!!
        TestAnalytic.of(UUID.randomUUID(), "Pretty easy huh?").track();

        // Since Grafiq runs on another thread, we gotta do some waiting until it's done here until proper testing is implemented, quick and dirty fix. Sorry guys :((
        Thread.sleep(3000);

        // Be sure to shut down after you're done!
        client.shutdown();
    }


    @GrafiqAnalytic(table = "test_table")
    public record TestAnalytic(UUID uuid, String testMessage) implements GrafiqInterface {
        
        // This is completely optional, I do it because I find it cool.
        public static TestAnalytic of(UUID uuid, String testMessage) {
            return new TestAnalytic(uuid, testMessage);
        }
        
    }
    
}

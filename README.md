<img width="1288" height="206" alt="image" src="https://github.com/user-attachments/assets/ff3a9ed4-51ab-4db0-b257-362dfcb64fd9" />

Grafiq is a Java analytics/event tracking library for ClickHouse, designed for easy mapping of Java records to ClickHouse tables. It supports automatic table creation, type mapping, and batch or single event tracking.
Originally made for the Mineburg server, but since we're nice we've opensourced it!

## Features

- **Automatic Table Creation:** Annotate your analytic records and Grafiq will create the corresponding ClickHouse tables.
- **Type Mapping:** Java types are mapped to ClickHouse types out of the box, with support for custom mappings (somewhat).
- **Batch & Single Event Tracking:** Track events individually or in batches.
- **Package Scanning:** Automatically discovers and registers analytic records in a package. Although, manual registering is also supported.

## Getting Started

### Requirements
*The library runs on Java 17*

### Installation
### Usage
*Check the JUnit tests for more accurate code.*

#### 1. Define an Analytic Record

```java
import nl.mineburg.grafiq.annotation.GrafiqAnalytic;
import nl.mineburg.grafiq.interfaces.GrafiqInterface;
import java.util.UUID;

@GrafiqAnalytic(table = "test_table")
public record TestAnalytic(UUID uuid, String testMessage) implements GrafiqInterface {
    public static TestAnalytic of(UUID uuid, String testMessage) {
        return new TestAnalytic(uuid, testMessage);
    }
}
```

#### 2. Initialize the Client

```java
import com.clickhouse.client.api.Client;
import nl.mineburg.grafiq.GrafiqClient;

GrafiqClient client = GrafiqClient.of(new Client.Builder()
    .addEndpoint("https://your-clickhouse-endpoint")
    .setUsername("default")
    .setPassword("your-password"));
```

#### 3. Process and Register Analytics

```java
client.process("your.package.path"); // e.g., "nl.mineburg.grafiq.test"
```

#### 4. Track Events

**Batch:**
```java
client.track(
    TestAnalytic.of(UUID.randomUUID(), "Message 1"),
    TestAnalytic.of(UUID.randomUUID(), "Message 2")
);
```

**List:**
```java
List<TestAnalytic> analytics = List.of(
    TestAnalytic.of(UUID.randomUUID(), "Message 3"),
    TestAnalytic.of(UUID.randomUUID(), "Message 4")
);
client.track(analytics);
```

**Single:**
```java
client.track(TestAnalytic.of(UUID.randomUUID(), "Single message"));
```

**Via Interface:**
```java
TestAnalytic.of(UUID.randomUUID(), "Easy!").track();
```

#### 5. Shutdown

```java
client.shutdown();
```

## License

MIT

---

Made by Willem & Sven for Mineburg.
```

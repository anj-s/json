# json

[![javadoc](https://javadoc.io/badge2/dev.mccue/json/javadoc.svg)](https://javadoc.io/doc/dev.mccue/json)
[![tests](https://github.com/bowbahdoe/json/actions/workflows/test.yml/badge.svg)](https://github.com/bowbahdoe/json/actions/workflows/test.yml)
<img src="./bopbop.png"></img>

A Java JSON Library intended to be easy to learn and simple to teach.

Requires Java 17+.

## Dependency Information

### Maven

```xml
<dependency>
    <groupId>dev.mccue</groupId>
    <artifactId>json</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Gradle

```
dependencies {
    implementation("dev.mccue:json:0.2.0")
}
```

## What this does

The primary goals of this library are
1. Be easy to learn and simple to teach.
2. Have an API for decoding that is reasonably declarative and gives good feedback
on unexpected input.
3. Make use of modern Java features.

The non-goals of this library are

1. Provide an API for data-binding.
2. Support every extension to the JSON spec.
3. Handle documents which cannot fit into memory.

## Examples and Explanation

## Data Model


### Reading from a String

```java
import dev.mccue.json.Json;

public class Main {
    public static void main(String[] args) {
        Json parsed = Json.readString("""
                {
                    "name": "Bop Bop",
                    "age": 1,
                    "cute": true
                }
                """);

        System.out.println(parsed);
    }
}
```

### Write to a String

```java
import dev.mccue.json.Json;

public class Main {
    public static void main(String[] args) {
        Json bopBop = Json.objectBuilder()
                .put("name", "Bop Bop")
                .put("age", 1)
                .put("cute", true)
                .build();
        
        String written = Json.writeString(bopBop);

        System.out.println(written);
    }
}
```

```
{"name":"Bop Bop","age":1,"cute":true}
```

### Write to a String with indentation

```java
import dev.mccue.json.Json;
import dev.mccue.json.JsonWriteOptions;

public class Main {
    public static void main(String[] args) {
        Json bopBop = Json.objectBuilder()
                .put("name", "Bop Bop")
                .put("age", 1)
                .put("cute", true)
                .build();
        
        String written = Json.writeString(
                bopBop,
                new JsonWriteOptions()
                        .withIndentation(4)
        );

        System.out.println(written);
    }
}
```

```
{
    "name": "Bop Bop",
    "age": 1,
    "cute": true
}
```



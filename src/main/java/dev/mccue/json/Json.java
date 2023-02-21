package dev.mccue.json;

import dev.mccue.json.internal.*;
import dev.mccue.json.stream.JsonStreamReadOptions;
import dev.mccue.json.stream.JsonValueHandler;
import dev.mccue.json.stream.JsonWriteable;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *     Immutable tree representation of Json.
 * </p>
 *
 * <p>
 *     There are no shared methods between the various implementors, so this class just serves the purpose
 *     of giving them a common supertype.
 * </p>
 */
public sealed interface Json
        extends Serializable, JsonEncodable, JsonWriteable
        permits JsonBoolean, JsonNull, JsonString, JsonNumber, JsonArray, JsonObject {
    static Json of(JsonEncodable value) {
        if (value == null) {
            return JsonNull.instance();
        }
        else {
            var asJson = value.toJson();
            if (asJson == null) {
                return JsonNull.instance();
            }
            else {
                return asJson;
            }
        }
    }

    static Json of(BigDecimal value) {
        return value == null ? JsonNull.instance() : new BigDecimalImpl(value);
    }

    static Json of(double value) {
        return new DoubleImpl(value);
    }

    static Json of(long value) {
        return new LongImpl(value);
    }

    static Json of(float value) {
        return new DoubleImpl(value);
    }

    static Json of(int value) {
        return new LongImpl(value);
    }

    static Json of(java.lang.Double value) {
        return value == null ? JsonNull.instance() : new DoubleImpl(value);
    }

    static Json of(java.lang.Long value) {
        return value == null ? JsonNull.instance() : new LongImpl(value);
    }

    static Json of(Float value) {
        return value == null ? JsonNull.instance() : new DoubleImpl(value);
    }

    static Json of(Integer value) {
        return value == null ? JsonNull.instance() : new LongImpl(value);
    }

    static Json of(java.math.BigInteger value) {
        return value == null ? JsonNull.instance() : new BigIntegerImpl(value);
    }

    static Json of(java.lang.String value) {
        return value == null ? JsonNull.instance() : new StringImpl(value);
    }

    static Json ofNull() {
        return JsonNull.instance();
    }

    static Json ofTrue() {
        return JsonBoolean.of(true);
    }

    static Json ofFalse() {
        return JsonBoolean.of(false);
    }


    static Json of(boolean b) {
        return JsonBoolean.of(b);
    }

    static Json of(java.lang.Boolean b) {
        return b == null ? JsonNull.instance() : JsonBoolean.of(b);
    }

    static Json of(Collection<? extends JsonEncodable> jsonList) {
        return jsonList == null
                ? JsonNull.instance()
                : new ArrayImpl(
                        jsonList.stream()
                                .map(json -> json == null ? JsonNull.instance() : json.toJson())
                                .toList()
                );
    }

    static Json of(Map<java.lang.String, ? extends JsonEncodable> jsonMap) {
        return jsonMap == null
                ? JsonNull.instance()
                : new ObjectImpl(
                        jsonMap
                                .entrySet()
                                .stream()
                                .collect(Collectors.toUnmodifiableMap(
                                        Map.Entry::getKey,
                                        entry -> entry.getValue() == null
                                                ? JsonNull.instance()
                                                : entry.getValue().toJson()
                                ))
                );
    }

    static JsonObject.Builder objectBuilder() {
        return JsonObject.builder();
    }

    static JsonObject.Builder objectBuilder(Map<java.lang.String, ? extends JsonEncodable> object) {
        if (object instanceof JsonObject o) {
            return new ObjectBuilder(new LinkedHashMap<>(o));
        }
        else {
            var objectEntries = new LinkedHashMap<String, Json>();
            for (var entry : object.entrySet()) {
                objectEntries.put(entry.getKey(), Json.of(entry.getValue()));
            }
            return new ObjectBuilder(objectEntries);
        }
    }

    /**
     * Convenience method to create a new array builder.
     *
     * @return A new array builder.
     */
    static JsonArray.Builder arrayBuilder() {
        return JsonArray.builder();
    }

    /**
     * Convenience method to create a new array builder from a collection
     * of {@link JsonEncodable} elements.
     *
     * @param elements A collection of elements which can be turned into {@link Json}.
     * @return A new {@link JsonArray.Builder}.
     */
    static JsonArray.Builder arrayBuilder(Collection<? extends JsonEncodable> elements) {
        if (elements instanceof JsonArray o) {
            return new ArrayBuilderImpl(new ArrayList<>(o));
        }
        else {
            return new ArrayBuilderImpl(new ArrayList<>(elements.stream()
                    .map(Json::of)
                    .toList()));
        }
    }

    /**
     * Convenience method for creating an empty array.
     *
     * @return An empty {@link JsonArray}.
     */
    static JsonArray emptyArray() {
        return ArrayImpl.EMPTY;
    }

    /**
     * Convenience method for creating an empty object.
     *
     * @return An empty {@link JsonObject}.
     */
    static JsonObject emptyObject() {
        return ObjectImpl.EMPTY;
    }

    @Override
    default Json toJson() {
        return this;
    }


    static Json readString(CharSequence jsonText) throws JsonReadException {
        return readString(jsonText, new JsonReadOptions());
    }

    static Json readString(CharSequence jsonText, JsonReadOptions options) throws JsonReadException {
        try {
            return JsonReaderMethods.readFullyConsume(new PushbackReader(
                    new StringReader(jsonText.toString()), JsonReaderMethods.MINIMUM_PUSHBACK_BUFFER_SIZE
            ), options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Json read(Reader reader, JsonReadOptions options) throws IOException, JsonReadException {
        return JsonReaderMethods.readFullyConsume(
                new PushbackReader(reader, JsonReaderMethods.MINIMUM_PUSHBACK_BUFFER_SIZE),
                options
        );
    }

    static Json read(Reader reader) throws IOException, JsonReadException {
        return read(reader, new JsonReadOptions());
    }

    static JsonReader reader(Reader reader, JsonReadOptions options) {
        var pushbackReader = new PushbackReader(
                reader,
                JsonReaderMethods.MINIMUM_PUSHBACK_BUFFER_SIZE
        );
        return () -> {
            try {
                return JsonReaderMethods.read(pushbackReader, options);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    static JsonReader reader(Reader reader) {
        return reader(reader, new JsonReadOptions()
                .withEOFBehavior(JsonReadOptions.EOFBehavior.RETURN_NULL));
    }

    static void readStream(Reader reader, JsonValueHandler handler, JsonStreamReadOptions options) throws IOException, JsonReadException {
        JsonReaderMethods.readStream(
                new PushbackReader(reader, JsonReaderMethods.MINIMUM_PUSHBACK_BUFFER_SIZE),
                false,
                options,
                handler
        );
    }

    static void readStream(Reader reader, JsonValueHandler handler) throws IOException, JsonReadException {
        JsonReaderMethods.readStream(
                new PushbackReader(reader, JsonReaderMethods.MINIMUM_PUSHBACK_BUFFER_SIZE),
                false,
                new JsonStreamReadOptions(),
                handler
        );
    }

    private static String writeString(Json json) {
        return writeString(json, new JsonWriteOptions());
    }

    private static String writeString(Json json, JsonWriteOptions options) {
        var sw = new StringWriter();
        try {
            write(json, sw, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sw.toString();
    }

    static String writeString(JsonEncodable jsonEncodable) {
        return writeString(Json.of(jsonEncodable));
    }

    static String writeString(JsonEncodable jsonEncodable, JsonWriteOptions options) {
        return writeString(Json.of(jsonEncodable), options);
    }

    private static void write(Json json, Writer writer, JsonWriteOptions options) throws IOException {
        new JsonWriter().write(json, writer, options);
    }

    private static void write(Json json, Writer writer) throws IOException {
        new JsonWriter().write(json, writer, new JsonWriteOptions());
    }

    static void write(JsonEncodable jsonEncodable, Writer writer, JsonWriteOptions options) throws IOException {
        write(Json.of(jsonEncodable), writer, options);
    }

    static void write(JsonEncodable jsonEncodable, Writer writer) throws IOException {
        write(Json.of(jsonEncodable), writer);
    }
}

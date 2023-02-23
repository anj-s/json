package dev.mccue.json;

import dev.mccue.json.internal.*;
import dev.mccue.json.stream.JsonStreamReadOptions;
import dev.mccue.json.stream.JsonValueHandler;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *     Immutable tree representation of the json data model.
 * </p>
 *
 * <p>
 *     The allowed implementors represent the different possible shapes Json can take.
 * </p>
 *
 * <ul>
 *     <li>{@link JsonObject} - A map of {@link String} to {@link Json}.</li>
 *     <li>{@link JsonArray} - An array of {@link Json}.</li>
 *     <li>{@link JsonString} - A string.</li>
 *     <li>{@link JsonNumber} - A number.</li>
 *     <li>{@link JsonTrue} - true.</li>
 *     <li>{@link JsonFalse} - false.</li>
 *     <li>{@link JsonNull} - null.</li>
 * </ul>
 *
 * <p>
 *     There are no shared methods between those implementors, so this class just serves the purpose
 *     of giving them a common supertype and to be a place for associated static methods.
 * </p>
 *
 * <p>
 *     Unless otherwise specified, all factory methods in this interface are null-safe and will replace
 *     any actual nulls with {@link JsonNull}.
 * </p>
 */
public sealed interface Json
        extends Serializable, JsonEncodable
        permits JsonBoolean, JsonNull, JsonString, JsonNumber, JsonArray, JsonObject {
    /**
     * Creates {@link Json} from something which implements {@link JsonEncodable},
     * including {@link Json} itself.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
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

    /**
     * Creates {@link Json} from a {@link BigDecimal}.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(BigDecimal value) {
        return value == null ? JsonNull.instance() : new BigDecimalImpl(value);
    }

    /**
     * Creates {@link Json} from a double.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(double value) {
        return new DoubleImpl(value);
    }

    /**
     * Creates {@link Json} from a long.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(long value) {
        return new LongImpl(value);
    }

    /**
     * Creates {@link Json} from a float.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(float value) {
        return new DoubleImpl(value);
    }

    /**
     * Creates {@link Json} from an int.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(int value) {
        return new LongImpl(value);
    }

    /**
     * Creates {@link Json} from a {@link Double}.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(Double value) {
        return value == null ? JsonNull.instance() : new DoubleImpl(value);
    }

    /**
     * Creates {@link Json} from a {@link Long}.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(Long value) {
        return value == null ? JsonNull.instance() : new LongImpl(value);
    }

    /**
     * Creates {@link Json} from a {@link Float}.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(Float value) {
        return value == null ? JsonNull.instance() : new DoubleImpl(value);
    }

    /**
     * Creates {@link Json} from an {@link Integer}.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(Integer value) {
        return value == null ? JsonNull.instance() : new LongImpl(value);
    }

    /**
     * Creates {@link Json} from a {@link BigInteger}.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(BigInteger value) {
        return value == null ? JsonNull.instance() : new BigIntegerImpl(value);
    }

    /**
     * Creates {@link Json} from a {@link String}.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(String value) {
        return value == null ? JsonNull.instance() : new StringImpl(value);
    }

    /**
     * Creates {@link Json} representing null.
     * @return {@link Json} representing null.
     */
    static Json ofNull() {
        return JsonNull.instance();
    }

    /**
     * Creates {@link Json} representing true.
     * @return {@link Json} representing true.
     */
    static Json ofTrue() {
        return JsonBoolean.of(true);
    }

    /**
     * Creates a {@link Json} representing false.
     * @return {@link Json} representing false.
     */
    static Json ofFalse() {
        return JsonBoolean.of(false);
    }


    /**
     * Creates {@link Json} from a boolean.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(boolean value) {
        return JsonBoolean.of(value);
    }

    /**
     * Creates {@link Json} from a {@link Boolean}.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(Boolean value) {
        return value == null ? JsonNull.instance() : JsonBoolean.of(value);
    }

    /**
     * Creates {@link Json} from a {@link Collection} of items which
     * implement {@link JsonEncodable}.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(Collection<? extends JsonEncodable> value) {
        return value == null
                ? JsonNull.instance()
                : new ArrayImpl(
                        value.stream()
                                .map(json -> json == null ? JsonNull.instance() : json.toJson())
                                .toList()
                );
    }

    /**
     * Creates {@link Json} from a {@link Map} with {@link String} keys to values which
     * implement {@link JsonEncodable}.
     *
     * @param value The value to be encoded.
     * @return An instance of {@link Json}.
     */
    static Json of(Map<String, ? extends JsonEncodable> value) {
        return value == null
                ? JsonNull.instance()
                : new ObjectImpl(
                        value
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

    /**
     * Creates a new {@link JsonObject.Builder}.
     *
     * @return A new {@link JsonObject.Builder}.
     */
    static JsonObject.Builder objectBuilder() {
        return JsonObject.builder();
    }

    /**
     * Creates a new {@link JsonObject.Builder} pre-filled with elements from a {@link Map}
     * with {@link String} keys and values which implement {@link JsonEncodable}.
     *
     * @param value The value to pre-fill the builder with.
     * @return A new {@link JsonObject.Builder}.
     */
    static JsonObject.Builder objectBuilder(Map<String, ? extends JsonEncodable> value) {
        if (value instanceof JsonObject o) {
            return new ObjectBuilder(new LinkedHashMap<>(o));
        }
        else {
            var objectEntries = new LinkedHashMap<String, Json>();
            for (var entry : value.entrySet()) {
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

    /**
     * Vacuous implementation to make methods like {@link Json#of(java.util.Collection)}
     * and {@link Json#of(java.util.Map)} convenient to create.
     *
     * @return Itself.
     */
    @Override
    default Json toJson() {
        return this;
    }


    /**
     * Reads the given text as {@link Json}
     *
     * <p>
     *     Only expects to read a single form. Will throw if there is non-whitespace content
     *     at the end.
     * </p>
     *
     * @param jsonText The text to read.
     * @return {@link Json}
     * @throws JsonReadException If the input {@link Json} is malformed.
     */
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

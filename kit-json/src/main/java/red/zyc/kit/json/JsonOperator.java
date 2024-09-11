/*
 * Copyright 2024 allurx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package red.zyc.kit.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import red.zyc.kit.base.constant.DateTimeFormatConstants;
import red.zyc.kit.base.reflection.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * JSON operator interface that defines basic JSON operations.
 *
 * @param <J> the type of the JSON processing entity (e.g., {@link ObjectMapper}, {@link Gson})
 * @author allurx
 * @see Gson
 * @see ObjectMapper
 */
public interface JsonOperator<J> extends JsonOperation {

    /**
     * Returns the current JSON processing entity, such as {@link ObjectMapper} or {@link Gson}.
     *
     * @return the JSON processing entity
     */
    J subject();

    /**
     * Creates a new {@link JsonOperator} using the provided JSON processing entity.
     *
     * @param supplier supplies the JSON processing entity
     * @return a new {@link JsonOperator} instance; implementations must return a new instance
     * to avoid unintended side effects when multiple threads use the same {@link JsonOperator}.
     */
    JsonOperator<J> with(Supplier<J> supplier);

    /**
     * Creates a new {@link JsonOperator} by applying a transformation to the current JSON processing entity.
     *
     * @param unaryOperator a function that applies transformations to the JSON processing entity
     * @return a new {@link JsonOperator} instance; implementations must return a new instance
     * to avoid unintended side effects when multiple threads use the same {@link JsonOperator}.
     */
    JsonOperator<J> with(UnaryOperator<J> unaryOperator);

    /**
     * Configures the JSON processing entity. This configuration will apply globally unless
     * a new instance is created using {@link #with(Supplier)} for specific configurations.
     *
     * @param consumer a configuration function for the JSON processing entity
     * @return the current instance for chaining calls
     */
    JsonOperator<J> configure(Consumer<J> consumer);

    /**
     * Copies properties from the source object to a new object of the specified {@link Type}.
     *
     * @param source the source object
     * @param type   the {@link Type} of the new object
     * @param <T>    the type of the new object
     * @return the new object with copied properties
     */
    <T> T copyProperties(Object source, Type type);

    /**
     * Copies properties from the source object to a new object of the specified {@link TypeToken#getType()}.
     *
     * @param source    the source object
     * @param typeToken the {@link TypeToken} representing the type of the new object
     * @param <T>       the type of the new object
     * @return the new object with copied properties
     */
    <T> T copyProperties(Object source, TypeToken<T> typeToken);

    /**
     * Compares multiple JSON strings to determine if they represent the same JSON object. This method is useful
     * when the JSON strings may have different formatting or escape characters that would affect direct string comparison.
     *
     * @param jsons an array of JSON strings to compare; must contain at least one element
     * @return {@code true} if all JSON strings represent the same JSON object, otherwise {@code false}
     */
    boolean compare(String... jsons);

    /**
     * A default Jackson-based operator that supports serialization and deserialization of JDK 8 date/time types
     * and outputs pretty-printed JSON.
     *
     * @see DateTimeFormatConstants
     */
    JacksonOperator JACKSON_OPERATOR = new JacksonOperator(new ObjectMapper()).configure(objectMapper -> {

        JavaTimeModule javaTimeModule = new JavaTimeModule();

        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_DATE));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ISO_TIME));

        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_DATE));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ISO_TIME));

        objectMapper.registerModule(javaTimeModule);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    });

    /**
     * A default Gson-based operator that supports serialization and deserialization of JDK 8 date/time types
     * and outputs pretty-printed JSON.
     *
     * @see DateTimeFormatConstants
     */
    GsonOperator GSON_OPERATOR = new GsonOperator(new GsonBuilder()

            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME)))
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE)))
            .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_TIME)))

            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_DATE_TIME))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext) -> LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_DATE))
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, type, jsonDeserializationContext) -> LocalTime.parse(json.getAsString(), DateTimeFormatter.ISO_TIME))
            .serializeNulls()
            .setPrettyPrinting()
            .create());

}

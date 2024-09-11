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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * JSON operations using the Gson library.
 *
 * @author allurx
 */
public class GsonOperator extends AbstractJsonOperator<GsonOperator, Gson> {

    /**
     * Constructor.
     *
     * @param gson An instance of {@link Gson}
     */
    public GsonOperator(Gson gson) {
        super(gson);
    }

    @Override
    public GsonOperator with(Supplier<Gson> supplier) {
        return new GsonOperator(supplier.get());
    }

    @Override
    public JsonOperator<Gson> with(UnaryOperator<Gson> unaryOperator) {
        return new GsonOperator(unaryOperator.apply(subject));
    }

    @Override
    public String toJsonString(Object source) {
        return subject.toJson(source);
    }

    @Override
    public <T> T fromJsonString(String json, Type type) {
        return subject.fromJson(json, type);
    }

    @Override
    public boolean compare(String... jsons) {
        JsonElement first = JsonParser.parseString(jsons[0]);
        return IntStream.range(1, jsons.length).allMatch(i -> first.equals(JsonParser.parseString(jsons[i])));
    }
}


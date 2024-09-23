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
package red.zyc.kit.base.concurrency;

/**
 * Interface for functions used in polling operations.
 *
 * <p>Defines the contract for functions that can be used by a {@link Poller}. Implementations must provide the actual execution logic.</p>
 *
 * @author allurx
 * @see RunnableFunction
 * @see CallableFunction
 */
public sealed interface PollerFunction permits RunnableFunction, CallableFunction {

}

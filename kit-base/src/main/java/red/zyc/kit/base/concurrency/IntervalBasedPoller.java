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

import red.zyc.kit.base.constant.FunctionConstants;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author allurx
 */
public class IntervalBasedPoller extends AbstractPoller<IntervalBasedPoller> {

    protected Clock clock = Clock.systemDefaultZone();
    protected Duration duration = Duration.ZERO;
    protected Duration interval = Duration.ZERO;
    protected Sleeper sleeper = Sleeper.DEFAULT;
    protected Runnable timeoutAction = FunctionConstants.EMPTY_RUNNABLE;

    @Override
    public <A, B> PollResult<B> poll(A input,
                                     Function<? super A, ? extends B> function,
                                     Predicate<? super B> predicate) {
        int cnt = 0;
        B result;
        Instant endInstant = clock.instant().plus(duration);
        while (true) {

            cnt++;

            // Exit polling if the function execution was successful
            if (predicate.test(result = execute(input, function))) break;

            // Check if polling needs to stop based on the remaining time
            boolean timeout = clock.instant().plus(interval).isAfter(endInstant);
            if (timeout) {
                timeoutAction.run();
                break;
            }

            // Pause before the next polling attempt
            sleeper.sleep(interval);
        }
        return new PollResult<>(cnt, result);
    }

    public IntervalBasedPoller timing(Duration duration, Duration interval) {
        return timing(Clock.systemDefaultZone(), duration, interval);
    }

    public IntervalBasedPoller timing(Clock clock, Duration duration, Duration interval) {
        this.clock = Objects.requireNonNull(clock, "the clock must not be null");
        this.duration = Objects.requireNonNull(duration, "the duration must not be null");
        this.interval = Objects.requireNonNull(interval, "the interval must not be null");
        return this;
    }

    public IntervalBasedPoller sleeper(Sleeper sleeper) {
        this.sleeper = Objects.requireNonNull(sleeper, "the sleeper must not be null");
        return this;
    }

    public IntervalBasedPoller onTimeout(Runnable timeoutAction) {
        this.timeoutAction = Objects.requireNonNull(timeoutAction, "the timeoutAction must not be null");
        return this;
    }

}

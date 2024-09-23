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

/**
 * @author allurx
 */
public class IntervalBasedPoller<A, B> extends AbstractPoller<A, B, IntervalBasedPoller<A, B>> {

    protected Clock clock = Clock.systemDefaultZone();
    protected Duration duration = Duration.ZERO;
    protected Duration interval = Duration.ZERO;
    protected Sleeper sleeper = Sleeper.DEFAULT;
    protected Runnable timeoutAction = FunctionConstants.EMPTY_RUNNABLE;

    @Override
    public B polling() {
        B output;
        Instant endInstant = clock.instant().plus(duration);
        while (true) {

            // Execute the function
            output = execute();

            // Exit polling if the function execution was successful
            if (predicate.test(output)) return output;

            // Check if polling needs to stop based on the remaining time
            boolean timeout = clock.instant().plus(interval).isAfter(endInstant);
            if (timeout) {
                timeoutAction.run();
                break;
            }

            // Pause before the next polling attempt
            sleeper.sleep(interval);
        }
        return output;
    }

    public IntervalBasedPoller<A, B> timing(Duration duration, Duration interval) {
        return timing(Clock.systemDefaultZone(), duration, interval);
    }

    public IntervalBasedPoller<A, B> timing(Clock clock, Duration duration, Duration interval) {
        this.clock = clock;
        this.duration = duration;
        this.interval = interval;
        return this;
    }

    public IntervalBasedPoller<A, B> sleeper(Sleeper sleeper) {
        this.sleeper = sleeper;
        return this;
    }

    public IntervalBasedPoller<A, B> onTimeout(Runnable timeoutAction) {
        this.timeoutAction = timeoutAction;
        return this;
    }

}

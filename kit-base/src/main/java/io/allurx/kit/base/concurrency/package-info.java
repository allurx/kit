/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Synchronous polling with attempt or duration limits and replaceable sleeping.
 * {@link io.allurx.kit.base.concurrency.Poller.PollResult} reports attempts and the last value,
 * without claiming the termination condition matched. Configured ignored failures become null results.
 *
 * @author allurx
 */
package io.allurx.kit.base.concurrency;

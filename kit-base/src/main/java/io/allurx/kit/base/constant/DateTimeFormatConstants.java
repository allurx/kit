/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
package io.allurx.kit.base.constant;

/**
 * Shared date and time pattern strings; no formatter, locale or time zone is configured here.
 *
 * @author allurx
 */
public final class DateTimeFormatConstants {

    private DateTimeFormatConstants() {
    }

    /**
     * Date and 24-hour time through seconds, with no offset or fractional seconds.
     * With {@link java.time.format.DateTimeFormatter}, {@code yyyy} denotes year-of-era.
     */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

}

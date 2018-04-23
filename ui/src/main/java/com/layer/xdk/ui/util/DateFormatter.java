package com.layer.xdk.ui.util;

import java.text.DateFormat;
import java.util.Date;

/**
 * Implement to provide appropriately formatted {@link String}s to display {@link Date}s
 *
 * @see DefaultDateFormatter for a sample implementation
 */
public interface DateFormatter {
    /**
     * Provide a suitable time and date {@link String} for displaying the supplied {@link Date}
     *
     * @param date the {@link Date} to be formatted
     * @return a suitable time and date {@link String} for displaying the supplied {@link Date}
     */
    String formatTimeDay(Date date);

    /**
     * Format the supplied {@link Date} as a time string
     *
     * @param date the {@link Date} to be formatted
     * @return a {@link String} formatted to represent the given date as time
     */
    String formatTime(Date date);

    /**
     * Format the supplied {@link Date} as a time string using the given time and date formats
     *
     * @param date       the {@link Date} to be formatted
     * @param timeFormat the {@link DateFormat} to be used to format the time
     * @param dateFormat the {@link DateFormat} to be used to format the date
     * @return a suitably formatted {@link String} for displaying the supplied {@link Date}
     */
    @SuppressWarnings("unused")
    String formatTime(Date date, DateFormat timeFormat, DateFormat dateFormat);
}

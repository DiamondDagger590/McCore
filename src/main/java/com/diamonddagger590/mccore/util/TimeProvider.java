package com.diamonddagger590.mccore.util;

import com.diamonddagger590.mccore.CorePlugin;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * This time provider gives an easy way to wrap
 * <p>
 * To access a plugin's instance of this, use {@link CorePlugin#getTimeProvider()}.
 *
 * @param clock The clock instance provided by this instance.
 */
public record TimeProvider(@NotNull Clock clock) {

    /**
     * Gets a current {@link Instant} based on the underlying clock
     * used by this provider.
     *
     * @return A current {@link Instant} based on the underlying clock
     * used by this provider.
     */
    @NotNull
    public Instant now() {
        return Instant.now(clock);
    }

    /**
     * Gets a current {@link LocalDateTime} zoned to the provided {@link ZoneId}
     * to allow easy use of time zones.
     *
     * @param zone The time zone to use.
     * @return A current {@link LocalDateTime} zoned to the provided {@link ZoneId}.
     */
    @NotNull
    public LocalDateTime nowLocal(@NotNull ZoneId zone) {
        return LocalDateTime.ofInstant(now(), zone);
    }

}

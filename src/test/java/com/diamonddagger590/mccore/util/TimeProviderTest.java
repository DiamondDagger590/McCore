package com.diamonddagger590.mccore.util;

import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeProviderTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-06-15T12:30:00Z");
    private static final ZoneId UTC = ZoneOffset.UTC;

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    private TimeProvider createFixedProvider() {
        return new TimeProvider(Clock.fixed(FIXED_INSTANT, UTC));
    }

    @Test
    @DisplayName("Given a fixed clock, when calling now, then returns the fixed instant")
    void now_returnsFixedInstant_whenClockIsFixed() {
        TimeProvider provider = createFixedProvider();
        assertEquals(FIXED_INSTANT, provider.now());
    }

    @Test
    @DisplayName("Given a fixed clock, when calling now multiple times, then returns same instant")
    void now_returnsSameInstant_whenCalledMultipleTimes() {
        TimeProvider provider = createFixedProvider();
        assertEquals(provider.now(), provider.now());
    }

    @Test
    @DisplayName("Given a fixed clock at UTC, when calling nowLocal with UTC, then returns correct local datetime")
    void nowLocal_returnsCorrectDateTime_whenZonedToUtc() {
        TimeProvider provider = createFixedProvider();
        LocalDateTime expected = LocalDateTime.of(2025, 6, 15, 12, 30, 0);
        assertEquals(expected, provider.nowLocal(UTC));
    }

    @Test
    @DisplayName("Given a fixed clock at UTC, when calling nowLocal with offset zone, then returns zone-adjusted datetime")
    void nowLocal_returnsAdjustedDateTime_whenZonedToOffset() {
        TimeProvider provider = createFixedProvider();
        ZoneId plus5 = ZoneOffset.ofHours(5);
        LocalDateTime expected = LocalDateTime.of(2025, 6, 15, 17, 30, 0);
        assertEquals(expected, provider.nowLocal(plus5));
    }

    @Test
    @DisplayName("Given a fixed clock at UTC, when calling nowLocal with negative offset, then returns earlier datetime")
    void nowLocal_returnsEarlierDateTime_whenZonedToNegativeOffset() {
        TimeProvider provider = createFixedProvider();
        ZoneId minus3 = ZoneOffset.ofHours(-3);
        LocalDateTime expected = LocalDateTime.of(2025, 6, 15, 9, 30, 0);
        assertEquals(expected, provider.nowLocal(minus3));
    }

    @Test
    @DisplayName("Given a time provider, when calling clock, then returns the underlying clock")
    void clock_returnsUnderlyingClock_whenCalled() {
        Clock fixedClock = Clock.fixed(FIXED_INSTANT, UTC);
        TimeProvider provider = new TimeProvider(fixedClock);
        assertEquals(fixedClock, provider.clock());
    }
}

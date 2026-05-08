package org.feature.management.shared.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class DateMapperTest {

    private final DateMapper mapper = new DateMapper();

    @Test
    void mapInstantToOffsetDateTime_nullReturnsNull() {
        assertThat(mapper.map((Instant) null)).isNull();
    }

    @Test
    void mapOffsetDateTimeToInstant_nullReturnsNull() {
        assertThat(mapper.map((OffsetDateTime) null)).isNull();
    }

    @Test
    void mapInstantToOffsetDateTime_usesUtc() {
        Instant instant = Instant.parse("2026-01-02T03:04:05Z");
        OffsetDateTime odt = mapper.map(instant);
        assertThat(odt).isEqualTo(instant.atOffset(ZoneOffset.UTC));
        assertThat(odt.getOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void mapOffsetDateTimeToInstant_roundTrips() {
        OffsetDateTime odt = OffsetDateTime.parse("2026-01-02T03:04:05+02:00");
        Instant instant = mapper.map(odt);
        assertThat(instant).isEqualTo(odt.toInstant());
    }
}


package com.climbx.climbx.user.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record DailyHistoryResponseDto(

    LocalDate date,
    Long value
) {

    public DailyHistoryResponseDto(java.sql.Date date, Long value) {
        this(date.toLocalDate(), value);
    }
}
package com.climbx.climbx.user.dto;

import java.sql.Date;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record DailyHistoryResponseDto(

    LocalDate date,
    Integer value
) {

    public DailyHistoryResponseDto(Date date, Long value) {
        this(date.toLocalDate(), value.intValue());
    }

    public DailyHistoryResponseDto(Date date, Integer value) {
        this(date.toLocalDate(), value);
    }
}
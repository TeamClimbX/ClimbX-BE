package com.climbx.climbx.user.dto;

import java.sql.Date;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record DailyHistoryResponseDto(

    LocalDate date,
    Long value
) {

    public DailyHistoryResponseDto(Date date, Long value) {
        this(date.toLocalDate(), value);
    }
}
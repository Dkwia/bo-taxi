package com.bootaxi.contracts.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyStatisticsResponse(
        LocalDate date,
        long tripsCount,
        BigDecimal averagePrice
) {
}

package com.bootaxi.trip.repository;

import com.bootaxi.trip.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    @Query("select count(t), coalesce(avg(t.price), 0) from Trip t where t.createdAt >= :start and t.createdAt < :end")
    Object[] dailyStatistics(@Param("start") Instant start, @Param("end") Instant end);
}

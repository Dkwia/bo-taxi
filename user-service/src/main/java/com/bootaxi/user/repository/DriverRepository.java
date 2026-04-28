package com.bootaxi.user.repository;

import com.bootaxi.contracts.enums.DriverStatus;
import com.bootaxi.user.domain.Driver;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    boolean existsByEmail(String email);

    boolean existsByLicenseNumber(String licenseNumber);

    Optional<Driver> findByEmail(String email);

    @Query("select d.id from Driver d where d.status = :status order by d.createdAt asc")
    List<Long> findIdsByStatus(@Param("status") DriverStatus status, Pageable pageable);

    @Modifying
    @Query("update Driver d set d.status = :status where d.id = :driverId")
    int updateStatus(@Param("driverId") Long driverId, @Param("status") DriverStatus status);

    @Modifying
    @Query("update Driver d set d.status = :newStatus where d.id = :driverId and d.status = :expectedStatus")
    int updateStatusIfCurrent(@Param("driverId") Long driverId,
                              @Param("expectedStatus") DriverStatus expectedStatus,
                              @Param("newStatus") DriverStatus newStatus);
}

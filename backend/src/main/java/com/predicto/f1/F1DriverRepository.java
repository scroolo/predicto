package com.predicto.f1;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface F1DriverRepository extends JpaRepository<F1Driver, UUID> {

    List<F1Driver> findBySessionKey(Integer sessionKey);

    Optional<F1Driver> findByDriverNumberAndSessionKey(Integer driverNumber, Integer sessionKey);
}

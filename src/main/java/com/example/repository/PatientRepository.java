package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.PatientEntity;

public interface PatientRepository extends JpaRepository<PatientEntity, Integer> {
   boolean existsByNhsNumber(String nhsNumber);

   Optional<PatientEntity> findByNhsNumber(String nhsNumber);
}

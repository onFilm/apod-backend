package com.onfilm.apodbackend.repository;

import com.onfilm.apodbackend.model.Apod;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for managing {@link Apod} entities.
 * Extends {@link JpaRepository} to provide standard CRUD operations.
 */
@Repository
public interface ApodRepository extends JpaRepository<Apod, LocalDate> {
    List<Apod> findByTitleContainingIgnoreCaseOrExplanationContainingIgnoreCase(String title, String explanation, Pageable pageable);
}
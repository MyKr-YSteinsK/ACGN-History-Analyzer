package com.mykr.acgnhistoryanalyzer.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BackgroundJobRepository extends JpaRepository<BackgroundJob, String> {

    Optional<BackgroundJob> findFirstByStatusOrderByCreatedAtAsc(BackgroundJobStatus status);
}

package com.lifeline.emergency.repository;

import com.lifeline.emergency.entity.FeedbackReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackReportRepository extends JpaRepository<FeedbackReport, Long> {
    List<FeedbackReport> findAllByOrderByCreatedAtDesc();
    List<FeedbackReport> findByReportTypeOrderByCreatedAtDesc(String reportType);
    List<FeedbackReport> findByStatusOrderByCreatedAtDesc(String status);
    List<FeedbackReport> findByTargetIdAndReportTypeOrderByCreatedAtDesc(Long targetId, String reportType);
    List<FeedbackReport> findByReporterEmailOrderByCreatedAtDesc(String reporterEmail);
    List<FeedbackReport> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
    boolean existsByReporterEmailAndTargetIdAndReportType(String reporterEmail, Long targetId, String reportType);
    java.util.Optional<FeedbackReport> findByReporterEmailAndTargetIdAndReportType(String reporterEmail, Long targetId, String reportType);
    List<FeedbackReport> findByReporterEmailAndReportType(String reporterEmail, String reportType);
    long countByStatus(String status);
    long countByReportType(String reportType);
}


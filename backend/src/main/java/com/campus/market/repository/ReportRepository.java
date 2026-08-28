package com.campus.market.repository;
import com.campus.market.entity.Report;
import com.campus.market.entity.Enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ReportRepository extends JpaRepository<Report,Long> {
    List<Report> findByReporterIdOrderByCreatedAtDesc(Long userId);
    List<Report> findAllByOrderByCreatedAtDesc();
    long countByStatus(ReportStatus status);
}

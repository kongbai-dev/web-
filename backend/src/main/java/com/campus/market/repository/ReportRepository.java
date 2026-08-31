package com.campus.market.repository;
import com.campus.market.entity.Report;
import com.campus.market.entity.Enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
public interface ReportRepository extends JpaRepository<Report,Long> {
    List<Report> findByReporterIdOrderByCreatedAtDesc(Long userId);
    List<Report> findAllByOrderByCreatedAtDesc();
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from Report r where r.id=:id")
    Optional<Report> findForUpdate(@Param("id") Long id);
    long countByStatus(ReportStatus status);
}

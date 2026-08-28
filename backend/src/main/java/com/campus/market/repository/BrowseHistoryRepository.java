package com.campus.market.repository;
import com.campus.market.entity.BrowseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory,Long> {
    Optional<BrowseHistory> findByUserIdAndProductId(Long userId, Long productId);
    List<BrowseHistory> findTop30ByUserIdOrderByViewedAtDesc(Long userId);
}

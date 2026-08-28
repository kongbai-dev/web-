package com.campus.market.repository;
import com.campus.market.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ReviewRepository extends JpaRepository<Review,Long> {
    List<Review> findByTargetUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByOrderIdAndReviewerId(Long orderId, Long reviewerId);
    long countByTargetUserId(Long userId);
    @org.springframework.data.jpa.repository.Query("select coalesce(avg(r.rating),5.0) from Review r where r.targetUser.id=?1")
    Double averageForUser(Long userId);
}

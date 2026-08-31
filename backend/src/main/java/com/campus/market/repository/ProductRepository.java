package com.campus.market.repository;
import com.campus.market.entity.Product;
import com.campus.market.entity.Enums.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {
    @Query("select p from Product p join fetch p.seller join fetch p.category where p.id=:id")
    Optional<Product> findDetail(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Product p where p.id=:id")
    Optional<Product> findForUpdate(@Param("id") Long id);
    @Modifying(clearAutomatically=true, flushAutomatically=true)
    @Query("update Product p set p.viewCount=p.viewCount+1 where p.id=:id")
    int incrementViewCount(@Param("id") Long id);
    @Query("select p from Product p where (:keyword is null or lower(p.title) like lower(concat('%',:keyword,'%')) or lower(p.description) like lower(concat('%',:keyword,'%'))) and (:categoryId is null or p.category.id=:categoryId) and (:status is null or p.status=:status) and (:minPrice is null or p.price>=:minPrice) and (:maxPrice is null or p.price<=:maxPrice)")
    Page<Product> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("status") ProductStatus status, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);
    long countByStatus(ProductStatus status);
}

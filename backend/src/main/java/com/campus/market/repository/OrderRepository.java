package com.campus.market.repository;
import com.campus.market.entity.MarketOrder;
import com.campus.market.entity.Enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
public interface OrderRepository extends JpaRepository<MarketOrder,Long> {
    @Query("select o from MarketOrder o where o.buyer.id=?1 or o.seller.id=?1 order by o.createdAt desc")
    List<MarketOrder> findMine(Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select o from MarketOrder o where o.id=:id")
    Optional<MarketOrder> findForUpdate(@Param("id") Long id);
    long countByStatus(OrderStatus status);
    long countByBuyerIdOrSellerId(Long buyerId, Long sellerId);
}

package com.campus.market.entity;

import com.campus.market.entity.Enums.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="market_orders", indexes={@Index(name="idx_order_status", columnList="status"),@Index(name="idx_order_buyer_created", columnList="buyer_id,created_at"),@Index(name="idx_order_seller_created", columnList="seller_id,created_at"),@Index(name="idx_order_product", columnList="product_id")})
public class MarketOrder {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false, unique=true) public String orderNo;
    @ManyToOne(optional=false) public Product product;
    @ManyToOne(optional=false) public User buyer;
    @ManyToOne(optional=false) public User seller;
    @Column(nullable=false) public BigDecimal amount;
    public String tradeLocation;
    public String buyerNote;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public OrderStatus status;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime confirmedAt;
    public LocalDateTime paidAt;
    public LocalDateTime deliveredAt;
    public LocalDateTime receivedAt;
    public LocalDateTime completedAt;
    public LocalDateTime cancelledAt;
}

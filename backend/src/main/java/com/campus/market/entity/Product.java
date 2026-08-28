package com.campus.market.entity;

import com.campus.market.entity.Enums.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="products", indexes={@Index(name="idx_product_status", columnList="status"), @Index(name="idx_product_created", columnList="createdAt")})
public class Product {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false, fetch=FetchType.EAGER) public User seller;
    @ManyToOne(optional=false, fetch=FetchType.EAGER) public Category category;
    @Column(nullable=false, length=100) public String title;
    @Column(columnDefinition="TEXT") public String description;
    public BigDecimal originalPrice;
    @Column(nullable=false) public BigDecimal price;
    public String conditionLevel;
    @Enumerated(EnumType.STRING) public TradeType tradeType = TradeType.SALE;
    public String tradeLocation;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public ProductStatus status = ProductStatus.ON_SALE;
    public Integer viewCount = 0;
    public Integer favoriteCount = 0;
    @ElementCollection(fetch=FetchType.EAGER) @Column(name="image_url", length=500) public List<String> imageUrls = new ArrayList<>();
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();
}

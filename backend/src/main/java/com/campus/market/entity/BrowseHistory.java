package com.campus.market.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="browse_history", uniqueConstraints=@UniqueConstraint(columnNames={"user_id","product_id"}))
public class BrowseHistory {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) @JoinColumn(name="user_id") public User user;
    @ManyToOne(optional=false) @JoinColumn(name="product_id") public Product product;
    public LocalDateTime viewedAt = LocalDateTime.now();
}

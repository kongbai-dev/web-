package com.campus.market.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="reviews", uniqueConstraints=@UniqueConstraint(columnNames={"order_id","reviewer_id"}))
public class Review {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) @JoinColumn(name="order_id") public MarketOrder order;
    @ManyToOne(optional=false) @JoinColumn(name="reviewer_id") public User reviewer;
    @ManyToOne(optional=false) public User targetUser;
    public Integer rating;
    @Column(length=500) public String content;
    public LocalDateTime createdAt = LocalDateTime.now();
}

package com.campus.market.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="categories")
public class Category {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false, unique=true) public String name;
    public Integer sortOrder = 0;
    public Boolean active = true;
    public LocalDateTime createdAt = LocalDateTime.now();
}

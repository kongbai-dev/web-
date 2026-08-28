package com.campus.market.entity;

import com.campus.market.entity.Enums.ReportStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="reports")
public class Report {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public User reporter;
    @ManyToOne(optional=false) public Product product;
    @Column(nullable=false) public String reason;
    public String description;
    @Enumerated(EnumType.STRING) public ReportStatus status = ReportStatus.PENDING;
    @ManyToOne public User admin;
    public String handleResult;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime handledAt;
}

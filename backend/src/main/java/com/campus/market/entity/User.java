package com.campus.market.entity;

import com.campus.market.entity.Enums.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="users")
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false, unique=true, length=50) public String username;
    @JsonIgnore @Column(nullable=false) public String passwordHash;
    @Column(unique=true, length=30) public String studentNo;
    public String nickname;
    public String avatar;
    public String email;
    public String phone;
    public String campus;
    public String dormArea;
    public String bio;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public Role role = Role.USER;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public UserStatus status = UserStatus.ACTIVE;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();
}

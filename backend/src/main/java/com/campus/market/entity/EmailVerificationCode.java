package com.campus.market.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="email_verification_codes")
public class EmailVerificationCode {
    @Id @Column(length=100) public String email;
    @Column(nullable=false, length=255) public String codeHash;
    @Column(nullable=false) public LocalDateTime expiresAt;
    @Column(nullable=false) public LocalDateTime lastSentAt;
    @Column(nullable=false) public Integer attempts = 0;
    @Version public Long version;
}

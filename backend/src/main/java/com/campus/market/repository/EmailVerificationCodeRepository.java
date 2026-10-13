package com.campus.market.repository;

import com.campus.market.entity.EmailVerificationCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode,String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from EmailVerificationCode c where c.email=:email")
    Optional<EmailVerificationCode> findForUpdate(@Param("email") String email);
}

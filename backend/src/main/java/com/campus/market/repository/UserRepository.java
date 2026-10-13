package com.campus.market.repository;
import com.campus.market.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByStudentNo(String studentNo);
    boolean existsByEmailIgnoreCase(String email);
}

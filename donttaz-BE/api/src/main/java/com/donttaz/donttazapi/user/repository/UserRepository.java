package com.donttaz.donttazapi.user.repository;

import com.donttaz.donttazapi.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByCi(String ci);
    boolean existsByEmail(String email);
}

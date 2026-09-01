package com.matchlog.be.repository;

import com.matchlog.be.constant.user.Provider;
import com.matchlog.be.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // [POST /api/v1/auth/login] 이메일로 유저 조회
    Optional<User> findByEmail(String email);

    // [GET /api/v1/auth/check-email] 이메일 중복 체크 / [POST /api/v1/auth/signup] 회원가입 중복 검증
    boolean existsByEmail(String email);

    Optional<User> findByEmailAndProvider(String email, Provider provider);
}

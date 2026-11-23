package com.matchlog.be.repository;

import com.matchlog.be.constant.notification.DeviceType;
import com.matchlog.be.domain.notification.UserDeviceToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    Optional<UserDeviceToken> findByUser_IdAndDeviceType(Long userId, DeviceType deviceType);
}

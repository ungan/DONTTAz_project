package com.donttaz.donttazapi.user.config;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalTestUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalTestUserInitializer.class);
    private static final long DEMO_USER_ID = 1L;
    private static final String DEFAULT_NAME = "Local Test User";
    private static final String DEFAULT_PHONE = "01012345678";
    private static final String DEFAULT_DEVICE_ID = "local-seed-device";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String userKey;
    private final String password;
    private final String pinCode;

    public LocalTestUserInitializer(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${local.test-user.email}") String email,
            @Value("${local.test-user.user-key}") String userKey,
            @Value("${local.test-user.password}") String password,
            @Value("${local.test-user.pin}") String pinCode
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.userKey = userKey;
        this.password = password;
        this.pinCode = pinCode;
    }

    @Override
    public void run(ApplicationArguments args) {
        validateRequiredValue("local.test-user.email", email);
        validateRequiredValue("local.test-user.user-key", userKey);
        validateRequiredValue("local.test-user.password", password);
        validateRequiredValue("local.test-user.pin", pinCode);

        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from users where user_id = ?",
                Integer.class,
                DEMO_USER_ID
        );

        LocalDateTime now = LocalDateTime.now();
        String encodedPassword = passwordEncoder.encode(password);
        String encodedPinCode = passwordEncoder.encode(pinCode);

        if (count != null && count > 0) {
            jdbcTemplate.update(
                    """
                    update users
                       set email = ?,
                           password = ?,
                           pin_code = ?,
                           name = ?,
                           phone_number = ?,
                           ci = null,
                           device_id = ?,
                           ssafy_user_key = ?,
                           status = ?,
                           updated_at = ?
                     where user_id = ?
                    """,
                    email,
                    encodedPassword,
                    encodedPinCode,
                    DEFAULT_NAME,
                    DEFAULT_PHONE,
                    DEFAULT_DEVICE_ID,
                    userKey,
                    "ACTIVE",
                    now,
                    DEMO_USER_ID
            );
            log.info("Updated local test user. userId={} email={}", DEMO_USER_ID, email);
            return;
        }

        jdbcTemplate.update(
                """
                insert into users (
                    user_id, email, password, pin_code, name, phone_number,
                    ci, device_id, ssafy_user_key, status, created_at, updated_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                DEMO_USER_ID,
                email,
                encodedPassword,
                encodedPinCode,
                DEFAULT_NAME,
                DEFAULT_PHONE,
                null,
                DEFAULT_DEVICE_ID,
                userKey,
                "ACTIVE",
                now,
                now
        );
        log.info("Inserted local test user. userId={} email={}", DEMO_USER_ID, email);
    }

    private void validateRequiredValue(String propertyName, String value) {
        if (value == null || value.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, propertyName + " is required.");
        }
    }
}

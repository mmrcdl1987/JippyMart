package com.jippy.mart.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class ExternalUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public ExternalUserRepository(@Qualifier("externalUserJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Search user by email or phone number.
     * Prioritizes matching both/either based on available input.
     */
    /**
     * Search user by email, phone number, or UPI ID (fcmToken/vpa).
     */
    public Optional<String> findUserIdByEmailPhoneOrUpi(String email, String phone, String upiId) {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (email != null && !email.trim().isEmpty()) {
            conditions.add("email = ?");
            params.add(email.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            conditions.add("phoneNumber = ?");
            params.add(phone.trim());
        }
        if (upiId != null && !upiId.trim().isEmpty()) {
            conditions.add("firstName = ?"); // Change field name if UPI is stored in another column
            params.add(upiId.trim());
        }

        if (conditions.isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT id FROM users WHERE " + String.join(" OR ", conditions) + " LIMIT 1";

        List<String> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("id"),
                params.toArray()
        );

        return results.stream().findFirst();
    }

    /**
     * Inserts a new user record into external DB and returns the generated String ID.
     */
    public String createNewUser(String email, String phone, String upiId,String firebaseId) {
        String sql = "INSERT INTO users (email, phoneNumber, firstName,firebase_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, (email != null && !email.trim().isEmpty()) ? email : null);
            ps.setString(2, (phone != null && !phone.trim().isEmpty()) ? phone : null);
            ps.setString(3, (upiId != null && !upiId.trim().isEmpty()) ? upiId : null);
            ps.setString(4, (firebaseId != null && !firebaseId.trim().isEmpty()) ? firebaseId : null);
            return ps;
        }, keyHolder);

        return keyHolder.getKey() != null ? String.valueOf(keyHolder.getKey().longValue()) : null;
    }
}

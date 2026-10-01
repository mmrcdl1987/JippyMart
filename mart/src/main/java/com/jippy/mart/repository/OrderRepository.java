package com.jippy.mart.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.mart.dto.OrderRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderRepository(@Qualifier("externalUserJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String createOrder(OrderRequestDto order) {
        String sql = """
            INSERT INTO restaurant_orders (
                id, author, authorID,  createdAt,
                payment_method, products,  status, toPayAmount, txn_id, vendorID
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try{
            log.info("Product dto: {}", order.getProductsDto());
            // Serialize ProductsDto object to JSON String
            String productsJson = objectMapper.writeValueAsString(order.getProductsDto());

            jdbcTemplate.update(
                    sql,
                    order.getId(),
                    order.getAuthor(),
                    order.getAuthorID(),
                    order.getCreatedAt(),
                    order.getPayment_method(),
                    productsJson,
                    order.getStatus(),
                    order.getToPayAmount(),
                    order.getTxn_id(),
                    order.getVendorID()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error serializing products DTO to JSON", e);
        }

        return order.getId();
    }

    public int findMaxIdSuffix() {
        String sql = "SELECT MAX(CAST(SUBSTRING(id, 8) AS UNSIGNED)) FROM restaurant_orders WHERE id LIKE 'Jippy33%'";
        Integer max = jdbcTemplate.queryForObject(sql, Integer.class);
        return max != null ? max : 0;
    }

    public boolean existsByTxnId(String txnId) {
        if (txnId == null || txnId.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM restaurant_orders WHERE txn_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, txnId);
        return count != null && count > 0;
    }
}

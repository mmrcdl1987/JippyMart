package com.jippy.mart.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public class MartProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public MartProductRepository(@Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<ProductPriceMatch> findProductForPayment(BigDecimal amount) {
        String sql = """
                SELECT mart_products_id, outlet_id, mart_sub_category_id, product_name,
                       minimum_price, maximum_price
                FROM jippy_mart.mart_products
                WHERE minimum_price <= ? AND maximum_price >= minimum_price
                ORDER BY CASE WHEN ? <= maximum_price THEN 0 ELSE 1 END,
                         CASE WHEN ? <= maximum_price
                              THEN ? - minimum_price
                              ELSE ? - maximum_price
                         END,
                         mart_products_id
                LIMIT 1
                """;

        return jdbcTemplate.query(
                sql,
                resultSet -> {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }

                    BigDecimal minimumPrice = resultSet.getBigDecimal("minimum_price");
                    BigDecimal maximumPrice = resultSet.getBigDecimal("maximum_price");
                    BigDecimal productPrice = amount.compareTo(maximumPrice) <= 0
                            ? minimumPrice
                            : maximumPrice;

                    return Optional.of(new ProductPriceMatch(
                            resultSet.getInt("mart_products_id"),
                            resultSet.getInt("outlet_id"),
                            resultSet.getInt("mart_sub_category_id"),
                            resultSet.getString("product_name"),
                            productPrice,
                            amount.subtract(productPrice)
                    ));
                },
                amount, amount, amount, amount, amount
        );
    }

    public record ProductPriceMatch(
            int productId,
            int outletId,
            int subcategoryId,
            String productName,
            BigDecimal productPrice,
            BigDecimal taxAmount
    ) {
    }
}

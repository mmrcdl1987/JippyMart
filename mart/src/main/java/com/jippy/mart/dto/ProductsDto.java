package com.jippy.mart.dto;

import lombok.Data;

@Data
public class ProductsDto {

    private Integer productId;
    private String productName;
    private Integer quantity;
    private Integer subcategoryId;
    private float price;
}

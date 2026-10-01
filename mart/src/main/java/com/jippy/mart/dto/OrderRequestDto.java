package com.jippy.mart.dto;

import lombok.Data;

@Data
public class OrderRequestDto {

    private String id;
    private String author;
    private String authorID;
    private String createdAt;
    private String payment_method;
    private ProductsDto productsDto;
    private String status;
    private Float toPayAmount;
    private String txn_id;
    private String vendorID;
    private String tax_setting;


}

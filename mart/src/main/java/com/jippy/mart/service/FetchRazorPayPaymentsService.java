package com.jippy.mart.service;

import java.math.BigDecimal;

public interface FetchRazorPayPaymentsService {

    void fetchRazorPayPaymentsAndCreateOrder(String paymentId, BigDecimal amountInRupees, String email, String contact, String bankRrn, String upiId);
}

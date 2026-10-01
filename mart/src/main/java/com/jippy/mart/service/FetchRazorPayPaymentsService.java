package com.jippy.mart.service;

public interface FetchRazorPayPaymentsService {

    void fetchRazorPayPaymentsAndCreateOrder(String paymentId, float amountInRupees, String email, String contact, String bankRrn,String upiId);
}

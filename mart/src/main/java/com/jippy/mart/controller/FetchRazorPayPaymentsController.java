package com.jippy.mart.controller;

import com.jippy.mart.service.FetchRazorPayPaymentsService;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/mart/")
public class FetchRazorPayPaymentsController {

    @Value("${razorpay.key-secret}")
    private String razorPayKeySecret;

    @Value("${razorpay.key-id}")
    private String razorPayKeyId;

    private final FetchRazorPayPaymentsService fetchRazorPayPaymentsService;

    @Scheduled(cron = "*/10 * * * * *")
    @GetMapping("/fetchRazorPayPayments")
    public void fetchAndCreateOrders() throws Exception {
        RazorpayClient razorpay = new RazorpayClient(razorPayKeyId, razorPayKeySecret);

        JSONObject params = new JSONObject();
        params.put("count", 10); // Number of records to fetch
        // Optional filters: "from" and "to" epoch timestamps

        List<Payment> payments = razorpay.payments.fetchAll(params);

        log.info("=================payments==================={}",payments);

        for (Payment payment : payments) {
            String status = getNullableString(payment, "status");
            String paymentId = getNullableString(payment, "id");

            // 1. Amount (Convert paise to Rupees if required: 20000 paise = ₹200.00)
            Integer amountInPaise = payment.get("amount");
            BigDecimal amountInRupees = BigDecimal.valueOf(amountInPaise, 2);

            // 2. Customer Details
            String email = getNullableString(payment, "email");
            String contact = getNullableString(payment, "contact");

            // 3. Bank RRN / Transaction ID (Method-specific)
            String bankRrn = payment.get("acquirer_data") != null && payment.toJson().has("acquirer_data")
                    ? payment.toJson().getJSONObject("acquirer_data").optString("rrn", "N/A")
                    : "N/A";

            // Extract customer UPI ID (VPA)
            String upiId = getNullableString(payment, "vpa");

            // Process only captured payments
            if ("captured".equals(status)) {

                log.info("=================================={} {} {} {} {} {} {} ",status,paymentId,amountInRupees,email,contact,bankRrn,upiId);

                fetchRazorPayPaymentsService.fetchRazorPayPaymentsAndCreateOrder(paymentId, amountInRupees, email, contact, bankRrn,upiId);
            }
        }
    }
    private String getNullableString(Payment payment, String key) {
        if (payment.has(key) && !payment.toJson().isNull(key)) {
            Object val = payment.get(key);
            return val != null ? val.toString() : null;
        }
        return null;
    }
}

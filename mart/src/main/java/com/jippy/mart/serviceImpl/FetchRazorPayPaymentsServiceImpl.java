package com.jippy.mart.serviceImpl;

import com.jippy.mart.dto.OrderRequestDto;
import com.jippy.mart.dto.ProductsDto;
import com.jippy.mart.repository.ExternalUserRepository;
import com.jippy.mart.repository.MartProductRepository;
import com.jippy.mart.repository.MartProductRepository.ProductPriceMatch;
import com.jippy.mart.repository.OrderRepository;
import com.jippy.mart.service.FetchRazorPayPaymentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FetchRazorPayPaymentsServiceImpl implements FetchRazorPayPaymentsService {

    private final ExternalUserRepository externalUserRepository;
    private final OrderRepository orderRepository;
    private final MartProductRepository martProductRepository;


    @Override
    public void fetchRazorPayPaymentsAndCreateOrder(String paymentId, BigDecimal amountInRupees, String email,
            String contact, String bankRrn,String upiId) {

        // 1. Check if email or contact is provided
        String userId = null;
        boolean hasValidEmail = email != null && !email.trim().isEmpty();
        boolean hasValidContact = contact != null && !contact.trim().isEmpty();
        boolean hasValidUpi = upiId != null && !upiId.trim().isEmpty();

        // 1. Check if order already exists for this transaction ID
        if (orderRepository.existsByTxnId(paymentId)) {
            log.info("Order for txn_id {} already exists. Skipping creation.", paymentId);
            return; // Skip to next payment
        }

        var productMatch = martProductRepository.findProductForPayment(amountInRupees);
        if (productMatch.isEmpty()) {
            log.warn("Skipping order for payment {}: no mart product has a minimum price at or below {}",
                    paymentId, amountInRupees);
            return;
        }
        ProductPriceMatch product = productMatch.get();

        String firebaseId = UUID.randomUUID().toString();

        // 2. Resolve User ID (Email / Phone / UPI)
        if (hasValidEmail || hasValidContact || hasValidUpi) {
            userId = externalUserRepository.findUserIdByEmailPhoneOrUpi(email, contact, upiId)
                    .orElseGet(() -> externalUserRepository.createNewUser(email, contact, upiId,firebaseId));

            log.info("UserId: {} for email: {}, contact: {}, upiId: {}", userId, email, contact, upiId);
        }

        if (userId == null) {
            log.warn("Skipping order creation for payment {}: Customer could not be determined", paymentId);
            return;
        }

        createOrder(userId, paymentId, amountInRupees, product);
    }

    private void createOrder(String userId, String paymentId, BigDecimal amountInRupees,
            ProductPriceMatch product) {
        String orderId = getOrderId();

        log.info("Creating order with ID: {} for User ID: {} and Payment ID: {}", orderId, userId, paymentId);

        OrderRequestDto order = new OrderRequestDto();
        order.setId(orderId);
        order.setAuthorID(userId);
        order.setPayment_method(paymentId);
        order.setToPayAmount(amountInRupees.floatValue());
        order.setTxn_id(paymentId);
        order.setStatus("Order Completed");
        order.setTaxSetting(product.taxAmount().stripTrailingZeros().toPlainString());
        order.setCreatedAt(Instant.now().toString());
        order.setVendorID(Integer.toString(product.outletId()));

        ProductsDto productsDto = new ProductsDto();
        productsDto.setProductName(product.productName());
        productsDto.setProductId(product.productId());
        productsDto.setPrice(product.productPrice().floatValue());
        productsDto.setQuantity(1);
        productsDto.setSubcategoryId(product.subcategoryId());

        order.setProductsDto(productsDto);

        // Save the order to the database
         orderId = orderRepository.createOrder(order);

        log.info("Order created with ID: {} for User ID: {}", orderId, userId);
    }

    private String getOrderId() {
        String prefix = "Jippy33";

        // 1. Get the current highest suffix number from DB
        Integer maxSuffix = orderRepository.findMaxIdSuffix();
        log.info("suffix for last inserted id : {} ",maxSuffix);

        int nextSuffix;
        if (maxSuffix == null) {
            // If the table is completely empty or no 'Jippy330' IDs exist yet, start at 4325
            nextSuffix = 4325;
        } else {
            // 2. Add plus 1 to the existing maximum number
            nextSuffix = maxSuffix + 1;
        }

        // 3. Format the next ID string (e.g., "Jippy3304326")
        // %08d ensures it pads with zeros to maintain 8 digits if needed (e.g. 0004326)
        // If your database strictly uses whatever length the number turns out to be, use: prefix + nextSuffix
        String nextId = prefix + String.format("%06d", nextSuffix);
        return  nextId;
    }
}

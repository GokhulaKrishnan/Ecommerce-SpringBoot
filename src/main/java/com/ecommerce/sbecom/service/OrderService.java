package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.payload.OrderDTO;

public interface OrderService {
    OrderDTO placeOrder(String email, Long addressId, String paymentMethod, String pgName, String pgStatus, String pgResponseMessage, String pgPaymentId);
}

package com.ecommerce.sbecom.controller;

import com.ecommerce.sbecom.payload.OrderDTO;
import com.ecommerce.sbecom.payload.OrderRequestDTO;
import com.ecommerce.sbecom.service.OrderService;
import com.ecommerce.sbecom.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private OrderService orderService;

    // For placing the order
    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO>  orderProducts(@PathVariable String paymentMethod, @RequestBody OrderRequestDTO orderRequestDTO) {

        // Getting the email of the current logged-in user.
        String email = authUtils.loggedInEmail();

        OrderDTO order = orderService.placeOrder(email, orderRequestDTO.getAddressId(), paymentMethod, orderRequestDTO.getPgName(),
                orderRequestDTO.getPgStatus(), orderRequestDTO.getPgResponseMessage(), orderRequestDTO.getPgPaymentId());

        return new ResponseEntity<OrderDTO>(order, HttpStatus.CREATED);
    }
}

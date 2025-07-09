package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.exceptions.APIExceptionHandler;
import com.ecommerce.sbecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sbecom.model.*;
import com.ecommerce.sbecom.payload.OrderDTO;
import com.ecommerce.sbecom.payload.OrderItemDTO;
import com.ecommerce.sbecom.repositories.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartServiceImpl cartServiceImpl;
    @Autowired
    private ModelMapper modelMapper;

    public OrderServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public OrderDTO placeOrder(String email, Long addressId, String paymentMethod, String pgName, String pgStatus, String pgResponseMessage, String pgPaymentId) {
        //    - Get the user cart

        Cart cart = cartRepository.findCartByEmail(email);

        if(cart==null){
            throw new ResourceNotFoundException("Cart", "email", email);
        }

        // Getting user address
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        //    - Create a new Order with the Payment info

        Order order = new Order();

        // Setting the order details
        order.setOrderDate(LocalDate.now());
        order.setEmail(email);
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted!");
        order.setAddress(address);

        // Now we need to set the payments
        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName );
        payment.setOrder(order);

        // Saving the payment first
        paymentRepository.save(payment);

        // Now setting the orderPayment
        order.setPayment(payment);

        // Now we can save the order
        Order savedOrder = orderRepository.save(order);

        //    - Get items from the cart into the order items

        // Here we are going to get the list of cart items and loop over it and for each cartItem, we are going to
        //      create an orderItem and push it in the list of OrderItems

        // Getting the cart Item from the carts
        List<CartItem> cartItems = cart.getCartItems();

        if(cartItems == null){
            throw new APIExceptionHandler("No cart items found");
        }

        // Creating a list of OrderItems which will contain all the order items
        List<OrderItem> orderItems = new ArrayList<>();

        for(CartItem cartItem : cartItems){
            OrderItem orderItem = new OrderItem();
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);

            // Adding to the orderItems
            orderItems.add(orderItem);
        }

        // Saving all the orderItem
        orderItems = orderItemRepository.saveAll(orderItems);

        //    - Update the product stock

        // We can make use of the cartItems to update the product stock
        cart.getCartItems().forEach(item -> {

            // Getting the quantity
            int quantity = item.getQuantity();

            // Getting the product
            Product product = item.getProduct();

            // Updating
            product.setQuantity(product.getQuantity() - quantity);

            // Saving
            productRepository.save(product);

            //    - Clear the cart

            // Here we can make use of the cartService delete
            cartServiceImpl.deleteProductFromCart(cart.getCartId(), item.getProduct().getProductId());
        });

        //    - Return the order summary

        // Converting entity into DTO
        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);


        // OrderDTO has list of OrderItems, so converting it into OrderItemsDTO
        orderItems.forEach(item -> {
            OrderItemDTO orderItemDTO = modelMapper.map(item, OrderItemDTO.class);
            orderDTO.getOrderItems().add(orderItemDTO);
        } );

        orderDTO.setAddressId(addressId);

        return orderDTO;
    }
}

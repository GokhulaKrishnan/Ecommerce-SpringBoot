package com.ecommerce.sbecom.controller;

import com.ecommerce.sbecom.model.Cart;
import com.ecommerce.sbecom.payload.CartDTO;
import com.ecommerce.sbecom.repositories.CartRepository;
import com.ecommerce.sbecom.service.CartService;
import com.ecommerce.sbecom.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    // Autowiring the cart service
    @Autowired
    private CartService cartService;
    @Autowired
    private AuthUtils authUtils;
    @Autowired
    private CartRepository cartRepository;

    // Creating an endpoint to add product to the database
    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId, @PathVariable Integer quantity) {

        // Passing it to the service layer
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);

        return new ResponseEntity<CartDTO>(cartDTO,HttpStatus.OK);
    }

    // Creating an endpoint to fetch all the carts of all the users
    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getAllCarts() {

        // Fetching it using service
        List<CartDTO> cartsDTO = cartService.getAllCarts();

        return new ResponseEntity<List<CartDTO>>(cartsDTO,HttpStatus.OK);
    }

    // Creating an endpoint to fetch users cart
    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getCart(){

        // Here we need to fetch the email id using AuthUtils
        String emailId = authUtils.loggedInEmail();

        // Getting the cart Id using the associated email
        Cart cart = cartRepository.findCartByEmail(emailId);
        Long cartId = cart.getCartId();

        CartDTO cartDTO = cartService.getCart(emailId, cartId);

        return new ResponseEntity<>(cartDTO,HttpStatus.OK);
    }

    // Creating an endpoint to update the product quantity
    @PutMapping("cart/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCart(@PathVariable Long productId, @PathVariable String operation) {

        // Using service layer
        CartDTO cartDTO = cartService.updateProduct(productId, operation.equalsIgnoreCase("delete") ? -1: 1);

        return new ResponseEntity<CartDTO>(cartDTO,HttpStatus.OK);
    }

    // Creating an endpoint to delete a product from the cart
    @DeleteMapping("/carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId, @PathVariable Long productId) {

        String status = cartService.deleteProductFromCart(cartId, productId);

        return new ResponseEntity<>(status,HttpStatus.OK);
    }
}

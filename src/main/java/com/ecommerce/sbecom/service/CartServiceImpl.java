package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.exceptions.APIExceptionHandler;
import com.ecommerce.sbecom.exceptions.APIResponse;
import com.ecommerce.sbecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sbecom.model.Cart;
import com.ecommerce.sbecom.model.CartItem;
import com.ecommerce.sbecom.model.Product;
import com.ecommerce.sbecom.payload.CartDTO;
import com.ecommerce.sbecom.payload.CartItemDTO;
import com.ecommerce.sbecom.payload.ProductDTO;
import com.ecommerce.sbecom.repositories.CartItemRepository;
import com.ecommerce.sbecom.repositories.CartRepository;
import com.ecommerce.sbecom.repositories.ProductRepository;
import com.ecommerce.sbecom.utils.AuthUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {

    // Autowiring Cart repository
    @Autowired
    private CartRepository cartRepository;

    // Autowiring Product repo
    @Autowired
    private ProductRepository productRepository;

    // Autowiring CartItem
    @Autowired
    private CartItemRepository cartItemRepository;

    // Autowiring Auth Utils
    @Autowired
    private AuthUtils authUtils;

    @Autowired
    ModelMapper modelMapper;

    // Implementing add Product to Cart
    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {

        // - 1. Get the cart associated with the user if not exist create one.
        Cart cart = createCart();

        // - 2. Get the product with product ID.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));


        // - 3. Perform validation on the product, check if there exist a product, stock.
        // Getting the Cart Item from the DB using cartId and productId.
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId);

        // If cartItem exist throw error
        if (cartItem != null) {
            throw new APIExceptionHandler("Product " + product.getProductName() + " already exists in the cart");
        }

        // If the quantity in the product is 0
        if(product.getQuantity() == 0) {
            throw new APIExceptionHandler("Product " + product.getProductName() + " has no quantity");
        }

        // if the quantity in the product is less than the given quantity
        if(product.getQuantity() < quantity) {
            throw new APIExceptionHandler("Please provide " + product.getProductName() +
                    " quantity less than or equal to " + product.getQuantity());
        }

        // - 4. Create a new cart item.
        CartItem newCartItem = new CartItem();


        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        newCartItem.setCart(cart);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        // - 5. Save the cart item.
        cartItemRepository.save(newCartItem);

        // If needed can update the stock quantity
        // product.setQuantity(product.getQuantity() - quantity);
        product.setQuantity(product.getQuantity());

        // Updating the total price
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));

        // Updating the cart's cart item with the new Cart item
        cart.getCartItems().add(newCartItem);

        cartRepository.save(cart);

        // - 6. Return the updated cart.
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        // Converting the products to productDTO to send via the cartDTO

        // Making a list of cartitems
        List<CartItem> cartItems = cart.getCartItems();


        // Converting
        Stream<ProductDTO> productDTOStream = cartItems.stream().map(item ->{
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            //Setting the quantity
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts((productDTOStream.toList()));
        return cartDTO;
    }


    // Helper function to get cart or create a new cart
    private Cart createCart(){

        // Getting the cart if available
        // Going to make use of Auth Utils to get the email
        Cart cart = cartRepository.findCartByEmail(authUtils.loggedInEmail());

        // Check if the cart exists
        if(cart != null){
            return cart;
        }

        // Creating a new Cart
        Cart newCart = new Cart();

        // Setting the price and the user
        newCart.setTotalPrice(0.00);
        newCart.setUser(authUtils.loggedInUser());

        // Saving the cart
        Cart resCart = cartRepository.save(newCart);

        return resCart;
    }

    // Method to get all the carts
    @Override
    public List<CartDTO> getAllCarts() {

        // Getting all the carts from the repository
        List<Cart> carts = cartRepository.findAll();

        // Adding vaidation to check if the cart is empty
        if(carts.isEmpty()){
            throw new APIExceptionHandler("No cart found");
        }



        // We need to convert the carts entity into carts DTO
        List<CartDTO> cartsDTO = carts.stream().map(cart ->{
            CartDTO cartDTO = modelMapper.map( cart, CartDTO.class);




            // Converting the cartItems into DTO
            List<ProductDTO> products = cart.getCartItems().stream().map(
                    product -> {
                        ProductDTO productDTO = modelMapper.map(product.getProduct(), ProductDTO.class);

                        // We also need to set the quantity correctly
                        productDTO.setQuantity(product.getQuantity());
                        return productDTO;
                    }
            ).toList();

            // Setting the productDTO
            cartDTO.setProducts(products);

            return cartDTO;
        } ).toList();

        return cartsDTO;
    }

    // Method to get users cart
    @Override
    public CartDTO getCart(String emailId, Long cartId) {

        // Getting the cart using both the email and cartId
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);

        // Validation to check whether the cart is empty
        if(cart == null){
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        // We need to convert it into CartDTO
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        // We also need to set the Product into ProductDTO in cartDTO
        List<ProductDTO> products = cart.getCartItems().stream().map(product -> {
            ProductDTO productDTO = modelMapper.map(product.getProduct(), ProductDTO.class);

            // Setting the product Quantity
            productDTO.setQuantity(product.getQuantity());
            return productDTO;
        }).toList();

        // Setting the products
        cartDTO.setProducts(products);

        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO updateProduct(Long productId, int quantity) {

        // 1. Get the Cart using the cartId which is got using emailId
        String emailId = authUtils.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(emailId);
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        // 2. Get the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // 3. Do validation for quantity
        if(product.getQuantity() == 0){
            throw new APIExceptionHandler("Product " + product.getProductName() + " has no quantity");
        }

        if(product.getQuantity() < quantity){
            throw new APIExceptionHandler("Please, make an order of the " + product.getProductName()
                    + " quantity less than or equal to " + product.getQuantity());
        }

        // 4. Get the cartItem using the productId and cartId
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(productId, cartId);

        // 5. Do validation
        if(cartItem == null){
            throw new APIExceptionHandler("Product " + product.getProductName() + " has no quantity");
        }

        int newQuantity = cartItem.getQuantity() + quantity;

        if (newQuantity < 0){
            throw new APIExceptionHandler("Product " + product.getProductName() + " has no quantity");
        }

        if (newQuantity == 0){
            deleteProductFromCart(productId, cartId);
        }else{
            // 6. Update the CartItem
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());

            // Updating the total Price of the cart
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));

            cartRepository.save(cart);
        }


        CartItem updatedCartItem = cartItemRepository.save(cartItem);

        // 7. Do validation
        if(updatedCartItem.getQuantity() <= 0){
            // Deleting the cartItem from the cart
            cartItemRepository.deleteById(updatedCartItem.getCartItemId());
        }

        // Converting it into DTO
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        // Converting the cartItem into DTO
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productDTOStream = cartItems.stream().map(item -> {
            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);

            // Updating the quantity
            productDTO.setQuantity(item.getQuantity());
            return productDTO;
        });

        // Set the productDTO in the cart
        cartDTO.setProducts(productDTOStream.toList());

        return cartDTO;
    }

    // Deleting a product from the cart.
    @Override
    @Transactional
    public String deleteProductFromCart(Long cartId, Long productId) {

        // 1. Validation

        // Check whether cart is available
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        // Check whether the cartItem is there
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId);

        if(cartItem == null){
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        // 2. Update the total price of the cart

        cart.setTotalPrice(cart.getTotalPrice() - cartItem.getProductPrice() * cartItem.getQuantity());

        // 3. Delete the cartItem

        cartItemRepository.deleteByCartIdAndProductId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " has been deleted";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {

        // Perform Validations
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        // Get the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // Get the Cart Item for the cart with the productId
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId);

        if (cartItem == null){
            throw new APIExceptionHandler("Product " + product.getProductName() + " has no quantity");
        }

        // Modifying the total price of the cart now

        // Removing the old price
        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        // Setting the new price of the product in the cartItem
        cartItem.setProductPrice(product.getSpecialPrice());

        // Now updating the total price in the cart
        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));


        // Saving the cartItem and cart
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
        
    }
}

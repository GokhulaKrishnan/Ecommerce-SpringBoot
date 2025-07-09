package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.exceptions.APIExceptionHandler;
import com.ecommerce.sbecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sbecom.model.Cart;
import com.ecommerce.sbecom.model.Category;
import com.ecommerce.sbecom.model.Product;
import com.ecommerce.sbecom.payload.CartDTO;
import com.ecommerce.sbecom.payload.ProductDTO;
import com.ecommerce.sbecom.payload.ProductResponse;
import com.ecommerce.sbecom.repositories.CartRepository;
import com.ecommerce.sbecom.repositories.CategoryRepository;
import com.ecommerce.sbecom.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    // Here we need to autowire product repo and category repo because both are related
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // We also need model mapper to convert
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CartRepository cartRepository;

    // Getting the File Service for image upload
    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;
    @Autowired
    private CartService cartService;
    @Autowired
    private CartServiceImpl cartServiceImpl;

    /// //////////////////

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize,  String order, String sortBy) {

        // Sort by and Order
        Sort sortAndOrderBy = order.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        // Using Pageable to create a container to fetch pages
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortAndOrderBy);

        // Using Page to get the particular page with the needed constraints
        Page<Product> pageProducts = productRepository.findAll(pageable);

        List<Product> products = pageProducts.getContent();

        // Throwing API Response if the product we get is null
        if(products.isEmpty()){
            throw new APIExceptionHandler("Product list is empty");
        }

        // Converting the list of Products into ProductDto using model mapper
        List<ProductDTO> productsDTO = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        // Creating a ProductRepsonse instance
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productsDTO);

        // Setting the meta data about the pages
        productResponse.setPageNumber(pageProducts.getTotalPages());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    /// /////////////////

    @Override
    public ProductResponse getAllProductsByCategory(Integer categoryId, Integer pageNumber, Integer pageSize,  String order, String sortBy) {

        // We first get the respective category from the categoryDb
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Sort by and Order
        Sort sortAndOrderBy = order.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        // Using Pageable to create a container to fetch pages
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortAndOrderBy);

        // Using Page to get the particular page with the needed constraints
        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category, pageable);

        // Using the above fetched Category to search in the productRepo
        List<Product> productsByCategory = pageProducts.getContent();

        // If the List of Products in empty we throw an exception
        if(productsByCategory.isEmpty()){
            throw new APIExceptionHandler("Product list is empty");
        }

        // Convert it into list of Product DTO
        List<ProductDTO> productDTOS = productsByCategory.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        // Putting it into product response
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);

        // Setting the meta data about the pages
        productResponse.setPageNumber(pageProducts.getTotalPages());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    /// /////////////////

    @Override
    public ProductResponse getAllProductsByKeyword(String keyword,  Integer pageNumber, Integer pageSize,  String order, String sortBy) {

        // Sort by and Order
        Sort sortAndOrderBy = order.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        // Using Pageable to create a container to fetch pages
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortAndOrderBy);

        // Using Page to get the particular page with the needed constraints
        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase("%"+ keyword +"%", pageable);

        // Using the above fetched Category to search in the productRepo
        List<Product> productsByCategory = pageProducts.getContent();

        // If the List of Products in empty we throw an exception
        if(productsByCategory.isEmpty()){
            throw new APIExceptionHandler("Product list is empty");
        }

        // Convert it into list of Product DTO
        List<ProductDTO> productDTOS = productsByCategory.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        // Putting it into product response
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);

        // Setting the meta data about the pages
        productResponse.setPageNumber(pageProducts.getTotalPages());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    /// /////////////////

    @Override
    public ProductDTO addProduct(Integer categoryId, ProductDTO productDTO) {


        // First we convert the given DTO to entity
        Product product = modelMapper.map(productDTO, Product.class);

        // With the given category id, we are fetching the category
        // If not found throwing an exception
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Exception: When the Product we are adding already present in the Db
        List<Product> productExist = productRepository.findByProductNameLikeIgnoreCase(product.getProductName());

        if(productExist != null && !productExist.isEmpty()){
            throw new APIExceptionHandler("Product already exists");
        }

        // Saving the category in the product
        product.setCategory(category);

        // Saving the product image with dummy
        product.setImage("dummy.png");

        // Calculating the specialPrice
        double specialPrice = product.getPrice() - ( (product.getDiscount()  * 0.01) * product.getPrice());

        // Adding the special price into the product
        product.setSpecialPrice(specialPrice);

        // Now saving it in the db
        Product savedProduct = productRepository.save(product);

        // Now converting the product entity into DTO to return to the client
        ProductDTO savedProductDTO = modelMapper.map(savedProduct, ProductDTO.class);

        // Returning the productDTO
        return savedProductDTO;
    }

    /// /////////////////

    // Updating the existing product
    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {

        // Coverting into Entity
        Product product = modelMapper.map(productDTO, Product.class);

        // First get the particular Product from the db
        Product productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));

        // Updating the attributes of the product
        productEntity.setProductId(productId);
        productEntity.setImage("dummy.png");
        productEntity.setPrice(product.getPrice());
        productEntity.setDiscount(product.getDiscount());
        productEntity.setCategory(product.getCategory());
        productEntity.setProductName(product.getProductName());
        productEntity.setDescription(product.getDescription());
        productEntity.setQuantity(product.getQuantity());

        // Since we are computing the specialDiscount on our own, we need to recompute it here
        double specialPrice = product.getPrice() - ( (product.getDiscount()  * 0.01) * product.getPrice());

        // Adding the special price into the product
        productEntity.setSpecialPrice(specialPrice);

        // Saving the productEntity in the db
        Product savedProductEntity = productRepository.save(productEntity);

        // HANDLING THE UPDATION IN THE CART

        // 1. Getting the carts which contains this product using the productId.

        List<Cart> carts = cartRepository.findCartByProductId(productId);

        // 2. Now we need to convert it into DTOS so that it is easy to work with

        List<CartDTO> cartDTOS = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            // Converting the Products
            List<ProductDTO> productDTOS = cart.getCartItems().stream().map(p ->
                 modelMapper.map(p.getProduct(), ProductDTO.class)
            ).toList();

            // Set the productsc
            cartDTO.setProducts(productDTOS);
            return cartDTO;
        }).toList();

        // 3. Use the cart Service to update the cart

        cartDTOS.forEach((cartDTO) -> {
            cartService.updateProductInCarts(cartDTO.getCartId(), productId);
        });

        // Converting the entity into DTO
        ProductDTO savedProductDTO = modelMapper.map(savedProductEntity, ProductDTO.class);

        return savedProductDTO;
    }

    /// /////////////////


    @Override
    public ProductDTO deleteProduct(Long productId) {

        // Getting the product from the db
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));

        // Deleting the product
        productRepository.delete(product);

        // DELETING THE PRODUCT FROM THE CART AFTER THE ORIGINAL PRODUCT IS DELETED

        List<Cart> carts = cartRepository.findCartByProductId(productId);

        // Using the cartService delete product to delete the products from the cart
        carts.forEach(cart -> {
            cartServiceImpl.deleteProductFromCart(cart.getCartId(), productId);
        });

        // Converting the Product to ProductDTO
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
        return productDTO;
    }

    /// /////////////////



    @Override
    public ProductDTO uploadProductImage(Long productId, MultipartFile file) throws IOException {

        // Checking whether the product exist
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));

        // Creating a desired path for the file to be saved and we pass it to the helper
        // String path = "image/";

        // Using the helper function to upload the image
        // String fileName = uploadImage(path, file);

        // Using the file Service for this
        String fileName = fileService.uploadImage(path, file);

        // Updating the image fileName in the product
        product.setImage(fileName);

        // Saving in the db
        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }


}

package com.ecommerce.sbecom.payload;

import com.ecommerce.sbecom.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private Long cartItemId;
    private Double price;
    private Integer quantity;
    private Double discount;
    private ProductDTO productDTO;
    private CartDTO cart;
}

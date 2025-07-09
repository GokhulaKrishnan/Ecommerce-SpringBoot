package com.ecommerce.sbecom.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long productId;

    @NotBlank
    @Size(min = 3, message = "The product name should be minimum of length 3")
    private String productName;

    @NotBlank
    @Size(min = 3, message = "The product description should be minimum of length 3")
    private String description;
    private String image;
    private double discount;
    private Integer quantity;
    private double price;
    private double specialPrice;

    // Adding a relationship
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // Relation for seller user.
    // This means that this is the owner side.
    // This is the products associated with the seller user.
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User user;


    // Relation for Cart items
    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private List<CartItem> products = new ArrayList<>();


}

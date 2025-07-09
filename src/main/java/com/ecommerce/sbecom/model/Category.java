package com.ecommerce.sbecom.model;

// This is used to represent the category
// Contains information about the category module

// Here we are creating the getters and setters and constructor

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Here we are going to mention this class as an entity which means this has to be represented as a table in db.
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int categoryId;

    @NotBlank
    @Size(min = 5, message = "There should be a minimum of 5 characters")
    private String categoryName;
}

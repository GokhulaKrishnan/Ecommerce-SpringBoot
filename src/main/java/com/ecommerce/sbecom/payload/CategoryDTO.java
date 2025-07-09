package com.ecommerce.sbecom.payload;

// This is similar to model but it is not model and is for the presentation layer for the client

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private int categoryId;
    private String categoryName;
}

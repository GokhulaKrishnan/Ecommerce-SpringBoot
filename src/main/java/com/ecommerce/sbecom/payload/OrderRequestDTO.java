package com.ecommerce.sbecom.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    private Long addressId;
    private Long paymentMethod;
    private String pgName;
    private String pgResponseMessage;
    private String pgStatus;
    private String pgPaymentId;

}

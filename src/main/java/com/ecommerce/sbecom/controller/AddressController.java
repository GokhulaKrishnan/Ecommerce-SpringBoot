package com.ecommerce.sbecom.controller;

import com.ecommerce.sbecom.model.User;
import com.ecommerce.sbecom.payload.AddressDTO;
import com.ecommerce.sbecom.service.AddressService;
import com.ecommerce.sbecom.utils.AuthUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AuthUtils authUtils;


    // Here we are creating an endpoint where the user address will be saved.
    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {

        // Getting the logged in user
        User currentUser = authUtils.loggedInUser();
        // Passing the given data to the address service
        AddressDTO savedAddress = addressService.createAddress(addressDTO, currentUser);

        return new ResponseEntity<AddressDTO>(savedAddress, HttpStatus.CREATED);
    }

    // Getting all the addresses from the system
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {

        List<AddressDTO> addresses = addressService.getAllAddresses();

        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    // Getting particular address by id
    @GetMapping("/addresses/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long id) {

        AddressDTO address = addressService.getAddressById(id);

        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    // Getting addresses for logged-in user
    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(){

        // Getting the current logged-in user
        User user = authUtils.loggedInUser();

        List<AddressDTO> addressDTOS = addressService.getUserAddress(user);

        return new ResponseEntity<>(addressDTOS, HttpStatus.OK);
    }

    // Updating the address
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long addressId, @RequestBody AddressDTO addressDTO) {

        AddressDTO updatedAddress = addressService.updateAddress(addressId, addressDTO);

        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    // Deleting the address
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long addressId) {

        String status = addressService.deleteAddress(addressId);

        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}

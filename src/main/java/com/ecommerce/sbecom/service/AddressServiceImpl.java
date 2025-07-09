package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.exceptions.APIExceptionHandler;
import com.ecommerce.sbecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sbecom.model.Address;
import com.ecommerce.sbecom.model.User;
import com.ecommerce.sbecom.payload.AddressDTO;
import com.ecommerce.sbecom.repositories.AddressRepository;
import com.ecommerce.sbecom.repositories.UserRepository;
import com.ecommerce.sbecom.utils.AuthUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    // Getting the address repos
    @Autowired
    private AddressRepository addressRepository;

    // Getting the user repository
    @Autowired
    private UserRepository userRepository;

    // Getting the AuthUtils to get the current logged-in user
    @Autowired
    private AuthUtils authUtils;

    // Getting the model mapper
    @Autowired
    ModelMapper modelMapper;


    // Method to create an address entity
    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        // Now we need to convert the address DTO into an entity
        Address address = modelMapper.map(addressDTO, Address.class);

        // Now we are updating the User side
        // Getting the list of addresses from the user
        List<Address> addresses = user.getAddresses();

//        // We need to check whether the list of address already contains the same address inorder to create the address
//        //      twice.
//        if(addresses.contains(address)) {
//            throw new APIExceptionHandler("Address already exists");
//        }

        // Adding the current address to the user address list
        addresses.add(address);

        // Set the updated address to the user address list
        user.setAddresses(addresses);

        // Updating the address on the address side
        address.setUser(user);

        // Updating the user repository
        userRepository.save(user);

        // Saving the address in the address repository
        Address savedAddress = addressRepository.save(address);

        // Converting it into DTO
        AddressDTO savedAddressDTO = modelMapper.map(savedAddress, AddressDTO.class);

        return savedAddressDTO;
    }


    // Method to get all the addresses
    @Override
    public List<AddressDTO> getAllAddresses() {

        List<Address> addresses = addressRepository.findAll();

        // Throwing API error is no address available
        if(addresses.isEmpty()){
            throw new APIExceptionHandler("No address found");
        }

        // Converting the addresses into DTO
        List<AddressDTO> addressDTOS = addresses.stream().map(address -> {
            AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
            return addressDTO;
        }).toList();

        return addressDTOS;
    }

    // Getting address by ID
    @Override
    public AddressDTO getAddressById(Long id) {

        // Getting the address
        Address address = addressRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Address", "AddressID", id));

        // Converting it into DTO
        AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
        return addressDTO;
    }

    // Getting address for the current logged-in user
    @Override
    public List<AddressDTO> getUserAddress(User user) {

        // Getting the user id
        Long userId = user.getUserId();

        List<Address> addresses = addressRepository.findAddressByUserId(userId);

        // Converting the address into DTO
        List<AddressDTO> addressDTOS = addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class)).toList();


        return addressDTOS;
    }

    // Updating the address
    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {

        // Getting the address by id
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "AddressID", addressId));

        // Converting DTO to entity
        Address toUpdateAddress =  modelMapper.map(addressDTO, Address.class);

        // Updating the address
        address.setCity(toUpdateAddress.getCity());
        address.setState(toUpdateAddress.getState());
        address.setStreet(toUpdateAddress.getStreet());
        address.setBuildingName(toUpdateAddress.getBuildingName());
        address.setCountry(toUpdateAddress.getCountry());
        address.setPincode(toUpdateAddress.getPincode());

        // Saving the address
        Address updatedAddress = addressRepository.save(address);

        // Updating the address in the user side
        User user = address.getUser();

        // Removing address which equals to the address id
        user.getAddresses().removeIf(addr -> addr.getAddressId().equals(addressId));
        user.getAddresses().add(address);

        userRepository.save(user);

        // Converting it into DTO
        AddressDTO savedAddressDTO = modelMapper.map(updatedAddress, AddressDTO.class);

        return savedAddressDTO;
    }

    @Override
    public String deleteAddress(Long addressId) {

        // Getting the address
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "AddressID", addressId));

        // Deleting the address
        addressRepository.delete(address);

        // Now we also need to delete the address from the user side
        User user = address.getUser();

        // Removing
        user.getAddresses().removeIf(addr -> addr.getAddressId().equals(addressId));
        userRepository.save(user);


        return "Deleted Address: " + address.getAddressId();
    }
}

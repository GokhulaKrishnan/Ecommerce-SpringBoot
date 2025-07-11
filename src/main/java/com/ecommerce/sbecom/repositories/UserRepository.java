package com.ecommerce.sbecom.repositories;

import com.ecommerce.sbecom.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByUserName(String username);

    boolean existsByUserName(@NotBlank @Size(max = 20) String userName);

    boolean existsByEmail(@NotBlank @Size(max = 50) @Email String email);
}

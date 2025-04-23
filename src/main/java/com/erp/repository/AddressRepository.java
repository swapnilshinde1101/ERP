package com.erp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.erp.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {
    // You can add custom query methods here if needed
}

package com.dufeng.module.address.service;

import com.dufeng.module.address.dto.AddressRequest;
import com.dufeng.module.address.entity.Address;
import java.util.List;

public interface AddressService {

    List<Address> list(Long userId);

    Address get(Long userId, Long id);

    Address create(Long userId, AddressRequest request);

    Address update(Long userId, Long id, AddressRequest request);

    void delete(Long userId, Long id);

    void setDefault(Long userId, Long id);

}

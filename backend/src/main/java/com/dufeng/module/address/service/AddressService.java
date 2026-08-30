package com.dufeng.module.address.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dufeng.common.constant.BusinessMessages;
import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.ResultCode;
import com.dufeng.module.address.dto.AddressRequest;
import com.dufeng.module.address.entity.Address;
import com.dufeng.module.address.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址管理。
 */
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressMapper addressMapper;

    public List<Address> list(Long userId) {
        return addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreateTime));
    }

    public Address get(Long userId, Long id) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, BusinessMessages.ADDRESS_NOT_FOUND);
        }
        return address;
    }

    @Transactional(rollbackFor = Exception.class)
    public Address create(Long userId, AddressRequest request) {
        if (Integer.valueOf(1).equals(request.getIsDefault())) {
            clearDefault(userId);
        }
        Address address = new Address();
        address.setUserId(userId);
        copy(request, address);
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        addressMapper.insert(address);
        return address;
    }

    @Transactional(rollbackFor = Exception.class)
    public Address update(Long userId, Long id, AddressRequest request) {
        Address address = get(userId, id);
        if (Integer.valueOf(1).equals(request.getIsDefault())) {
            clearDefault(userId);
        }
        copy(request, address);
        addressMapper.updateById(address);
        return address;
    }

    public void delete(Long userId, Long id) {
        get(userId, id);
        addressMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long id) {
        get(userId, id);
        clearDefault(userId);
        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .eq(Address::getId, id)
                .set(Address::getIsDefault, 1));
    }

    private void clearDefault(Long userId) {
        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));
    }

    private void copy(AddressRequest request, Address address) {
        address.setReceiver(request.getReceiver());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setIsDefault(request.getIsDefault() == null ? 0 : request.getIsDefault());
    }
}

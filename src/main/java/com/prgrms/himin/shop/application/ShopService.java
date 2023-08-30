package com.prgrms.himin.shop.application;

import com.prgrms.himin.shop.domain.Shop;
import com.prgrms.himin.shop.domain.ShopRepository;
import com.prgrms.himin.shop.dto.request.ShopCreateRequest;
import com.prgrms.himin.shop.dto.response.ShopResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopService {

    private final ShopRepository shopRepository;

    @Transactional
    public ShopResponse createShop(ShopCreateRequest request) {
        Shop shop = request.toEntity();
        Shop savedShop = shopRepository.save(shop);
        return ShopResponse.from(savedShop);
    }
}
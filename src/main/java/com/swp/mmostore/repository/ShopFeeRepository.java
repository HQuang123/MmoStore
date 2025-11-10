package com.swp.mmostore.repository;

import com.swp.mmostore.entity.ShopFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopFeeRepository extends JpaRepository<ShopFee, Integer> {

}
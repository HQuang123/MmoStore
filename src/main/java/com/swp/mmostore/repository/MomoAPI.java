package com.swp.mmostore.repository;

import com.swp.mmostore.entity.MomoQueryRequest;
import com.swp.mmostore.entity.MomoQueryResponse;
import com.swp.mmostore.entity.MomoRequest;
import com.swp.mmostore.entity.MomoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "momo", url = "${momo.endpoint}")
public interface MomoAPI {
    @PostMapping("/create")
    MomoResponse createMomoQR(@RequestBody MomoRequest request);

    @PostMapping("/query")
    MomoQueryResponse queryTransactionStatus(@RequestBody MomoQueryRequest request);
}

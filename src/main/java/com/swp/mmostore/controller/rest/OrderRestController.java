package com.swp.mmostore.controller.rest;

import com.swp.mmostore.dto.FilterDTO;
import com.swp.mmostore.dto.ProductSummaryDTO;
import com.swp.mmostore.entity.Order;
import com.swp.mmostore.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/user/api/order")
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Order createOrder(@RequestBody Map<String, Object> data, Principal principal) {
        String userEmail = principal.getName(); //user email
        int quantity = Integer.parseInt(data.get("quantity").toString());
        double totalPrice = Double.parseDouble(data.get("totalPrice").toString());
        Integer productId = Integer.parseInt(data.get("productId").toString());
        return orderService.createNewOrder(quantity, totalPrice, userEmail, productId);
    }
}

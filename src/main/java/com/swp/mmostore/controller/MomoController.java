package com.swp.mmostore.controller;

import com.swp.mmostore.dto.OrderForm;
import com.swp.mmostore.entity.*;
import com.swp.mmostore.repository.DepositRepository;
import com.swp.mmostore.repository.OrderRepository;
import com.swp.mmostore.repository.ProductRepository;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.service.DepositService;
import com.swp.mmostore.service.MomoService;
import com.swp.mmostore.service.OrderService;
import com.swp.mmostore.util.MockSecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@AllArgsConstructor
@Controller
@RequestMapping("/user/momo")
public class MomoController {
//    result code will be 0 if success and message will be Thanh cong
    private final MomoService momoService;
    private final OrderRepository orderRepository;
    private final DepositService depositService;
    private final DepositRepository depositRepository;

    @PostMapping("/top-up")
    public String createQRCode(@ModelAttribute Deposit deposit) {
        Deposit pendingDeposit = depositService.createPendingDeposit(deposit);
        //generate a QR code
        MomoResponse momoResponse=  momoService.createQr(pendingDeposit);
        return "redirect:" + momoResponse.getPayUrl();
    }

    @GetMapping("/redirect")
    public String handleMomoRedirect(@RequestParam(name = "orderId") String depositId,
                                     @RequestParam(name = "orderInfo") String depositInfo,

                                     @RequestParam(name = "amount") String amount,
                                     @RequestParam(name ="resultCode") String resultCode,
                                     @RequestParam(name = "message") String message,
                                     Model model
                                     ) {
        boolean isSuccess = resultCode.equals("0");
        model.addAttribute("depositId", depositId);
        model.addAttribute("depositInfo", depositInfo);
        model.addAttribute("amount", amount);
        model.addAttribute("message", message);
        model.addAttribute("isSuccess", isSuccess);
        Deposit deposit = depositRepository.findById(Integer.parseInt(depositId)).orElse(null);
        model.addAttribute("deposit", deposit);
        return "/user/momo-redirect";
    }
}

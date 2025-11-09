package com.swp.mmostore.controller;

import com.swp.mmostore.entity.*;
import com.swp.mmostore.repository.DepositRepository;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.service.DepositService;
import com.swp.mmostore.service.MomoService;
import com.swp.mmostore.util.MockSecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
@Slf4j
@RequiredArgsConstructor
@Controller
public class WalletController {
    private final UserRepository userRepository;
    private final MomoService momoService;
    private final DepositService depositService;
    private final DepositRepository depositRepository;

    @ModelAttribute //this method will automatically model.addAttribute for every method in this class, create one instance of deposit per request
    public void addCommonAttributes(Model model) {
        String email = MockSecurityUtils.getCurrentUserEmail();
        log.info("Current user id: {}", email);
        User user = userRepository.findByEmail(email);
        Deposit deposit = new Deposit();
        deposit.setUser(user);
        deposit.setActionType(ActionType.Top_up);
        deposit.setPaymentMethod("Momo");
        model.addAttribute("deposit", deposit);
        model.addAttribute("user", user);
    }

    @GetMapping("/user/wallet")
    public String viewWallet(Model model) {
        User user = (User) model.getAttribute("user");
        model.addAttribute("balance", user.getBalance());
        return "user/wallet"; // → renders wallet.html
    }

    @GetMapping("/user/wallet/top-up")
    public String topUpBalance(Model model) {
            // Simply return the top-up view
            return "user/top-up";
    }


    @PostMapping("/user/momo/top-up")
    //new object is created due to the @ModelAttribute annotation, takes the data from the form and bind to the object
    public String createQRCode(@Valid @ModelAttribute Deposit deposit, BindingResult bindingResult ) {
        if (bindingResult.hasErrors()) {
            return "user/top-up";
        }
        Deposit pendingDeposit = depositService.createPendingDeposit(deposit);
        //generate a QR code
        MomoResponse momoResponse=  momoService.createQr(pendingDeposit);
        return "redirect:" + momoResponse.getPayUrl();
    }

    @GetMapping("/momo/redirect")
    public String handleMomoRedirect(@RequestParam(name = "orderId") String depositId,
                                     @RequestParam(name = "orderInfo") String depositInfo,
                                     @RequestParam(name = "amount") String amount,
                                     @RequestParam(name ="resultCode") String resultCode,
                                     @RequestParam(name = "message") String message,
                                     Model model
    ) {
        log.info(">>>>> ket qua: {}" , resultCode);
        boolean isSuccess = resultCode.equals("0");
        model.addAttribute("depositId", depositId);
        model.addAttribute("depositInfo", depositInfo);
        model.addAttribute("amount", amount);
        model.addAttribute("message", message);
        model.addAttribute("isSuccess", isSuccess);
        Deposit deposit = depositRepository.findById(Integer.parseInt(depositId)).orElse(null);
        model.addAttribute("deposit", deposit);
        return "user/momo-redirect";
    }
}

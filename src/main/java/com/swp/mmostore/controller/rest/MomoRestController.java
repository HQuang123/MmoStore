package com.swp.mmostore.controller.rest;

import com.swp.mmostore.entity.*;
import com.swp.mmostore.repository.DepositRepository;
import com.swp.mmostore.repository.OrderRepository;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.service.DepositService;
import com.swp.mmostore.service.MomoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
@Slf4j
@RequiredArgsConstructor
//init final field

@RestController
@RequestMapping("api/momo")
public class MomoRestController {
    private final DepositService depositService;
    private final DepositRepository depositRepository;
    private final UserRepository userRepository;
    private final MomoService momoService;

    @PostMapping("create")
    public MomoResponse createQRCode() {
        Deposit deposit = new Deposit();
        deposit.setId(1);
        deposit.setActionType(ActionType.Top_up);
        deposit.setPaymentMethod("Momo");
        deposit.setAmount(BigDecimal.valueOf(100000));
        //generate a QR code
        return momoService.createQr(deposit);
    }
    //0 is success, !0 is failure

    @PostMapping("/ipn-handler")
    public ResponseEntity<Object> ipnHandler(@RequestBody Map<String, String> payload) {
        try{
            Integer depositId = Integer.parseInt(payload.get("orderId"));

            String resultCode = payload.get("resultCode");
            Deposit deposit = depositRepository.findById(depositId).orElse(null);
            log.info("Deposit Id la: {}" ,deposit.getId());
            User user = deposit.getUser();
            //TODO: implement orderService to mark success or failure
            if(resultCode.equals("0")) {
                //todo: implement
                deposit.setStatus(DepositStatus.Completed);
                user.setBalance(user.getBalance().add(deposit.getAmount()));
            }
            else{
                //Todo: implement here
                deposit.setStatus(DepositStatus.Failed);
            }
            userRepository.save(user);
            return ResponseEntity.noContent().build();
        }
        catch (Exception e){
            log.error("Loi khi lay request body tu ipn url + {}",e.getMessage());
            return ResponseEntity.noContent().build(); //momo needs to response back with 204 code to avoid spamming to BE api
        }
    }

}
package com.swp.mmostore.service;

import com.swp.mmostore.dto.TransactionDTO;
import com.swp.mmostore.entity.Deposit;
import com.swp.mmostore.entity.TransactionType;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import com.swp.mmostore.repository.DepositRepository;
import com.swp.mmostore.repository.WithdrawalRepository;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    public Page<TransactionDTO> getTransactionHistory(User user, Pageable pageable) {

        List<TransactionDTO> allTransactions = new ArrayList<>();

        // 1. Fetch all deposits and map them
        List<Deposit> deposits = depositRepository.findByUser(user);
        deposits.stream().forEach(d -> allTransactions.add(new TransactionDTO(
                "D_" + d.getId(),
                TransactionType.DEPOSIT,
                d.getAmount(),
                d.getStatus().toString(),
                d.getCreateAt(),
                "Nạp tiền qua MoMo"
        )));

        // 2. Fetch all withdrawals and map them
        List<Withdrawal> withdrawals = withdrawalRepository.findByUser(user);
        withdrawals.stream().forEach(w -> allTransactions.add(new TransactionDTO(
                "W_" + w.getId(),
                TransactionType.WITHDRAWAL,
                w.getAmount(),
                w.getStatus(),
                w.getCreateAt(),
                "Rút tiền về tài khoản ngân hàng"
        )));

        // 3. Sort the combined list based on Pageable's sort
        allTransactions.sort(getComparator(pageable.getSort()));

        // 4. Manually paginate the final list
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<TransactionDTO> pageList;

        if (allTransactions.size() < startItem) {
            pageList = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, allTransactions.size());
            pageList = allTransactions.subList(startItem, toIndex);
        }

        // 5. Create a PageImpl to return
        return new PageImpl<>(pageList, pageable, allTransactions.size());
    }

    // Helper method to create a dynamic comparator from Pageable's Sort
    private Comparator<TransactionDTO> getComparator(Sort sort) {
        Comparator<TransactionDTO> comparator = null;

        for (Sort.Order order : sort) {
            Comparator<TransactionDTO> currentComp = switch (order.getProperty()) {
                case "amount" -> Comparator.comparing(TransactionDTO::amount);
                case "status" -> Comparator.comparing(TransactionDTO::status);
                case "type" -> Comparator.comparing(t -> t.type().name());
                default -> Comparator.comparing(TransactionDTO::createAt);
            };
            if (order.isDescending()) {
                currentComp = currentComp.reversed();
            }
            comparator = (comparator == null) ? currentComp : comparator.thenComparing(currentComp);
        }

        // Default sort if none provided
        return comparator != null ? comparator : Comparator.comparing(TransactionDTO::createAt).reversed();
    }
}

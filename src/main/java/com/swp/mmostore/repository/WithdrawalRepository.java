package com.swp.mmostore.repository;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Integer>, JpaSpecificationExecutor<Withdrawal> {
    List<Withdrawal> findByStatus(String status);
    Optional<Withdrawal> findByUserAndStatus(User user, String status);
    List<Withdrawal> findByUserOrderByCreateAtDesc(User user);
    List<Withdrawal> findByUserAndStatusNot(User user, String status, Sort sort);

    List<Withdrawal> findByUser(User user);
}

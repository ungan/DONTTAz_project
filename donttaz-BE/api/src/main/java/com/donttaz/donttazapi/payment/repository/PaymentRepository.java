package com.donttaz.donttazapi.payment.repository;

import com.donttaz.donttazapi.payment.domain.Payment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByUserIdAndWithdrawalTransactionIdIn(Long userId, Collection<String> withdrawalTransactionIds);

    Optional<Payment> findByUserIdAndWithdrawalTransactionId(Long userId, String withdrawalTransactionId);
}

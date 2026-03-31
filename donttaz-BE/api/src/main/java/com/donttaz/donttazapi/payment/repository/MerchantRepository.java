package com.donttaz.donttazapi.payment.repository;

import com.donttaz.donttazapi.payment.domain.Merchant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    boolean existsBySettlementAccountNo(String settlementAccountNo);

    List<Merchant> findAllByActiveTrueOrderByNameAsc();

    Optional<Merchant> findByMerchantIdAndActiveTrue(Long merchantId);
}

package com.donttaz.donttazapi.ai.repository;

import com.donttaz.donttazapi.ai.domain.TransactionAiMetadata;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionAiMetadataRepository extends JpaRepository<TransactionAiMetadata, Long> {

    Optional<TransactionAiMetadata> findByTransactionId(Long transactionId);

    List<TransactionAiMetadata> findAllByTransactionIdIn(Collection<Long> transactionIds);
}

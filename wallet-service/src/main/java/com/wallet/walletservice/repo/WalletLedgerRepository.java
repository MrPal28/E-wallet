package com.wallet.walletservice.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wallet.walletservice.constants.LedgerType;
import com.wallet.walletservice.entity.WalletLedger;

public interface WalletLedgerRepository extends JpaRepository<WalletLedger, String> {
    Optional<WalletLedger> findByReferenceId(String referenceId);
    boolean existsByReferenceId(String referenceId);
    boolean existsByReferenceIdAndType(String referenceId, LedgerType credit);
}

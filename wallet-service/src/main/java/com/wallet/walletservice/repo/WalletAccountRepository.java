package com.wallet.walletservice.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;


import com.wallet.walletservice.entity.WalletAccount;

import jakarta.persistence.LockModeType;


public interface WalletAccountRepository extends JpaRepository<WalletAccount, Long> {

    Optional<WalletAccount> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletAccount w WHERE w.userId = :userId")
    Optional<WalletAccount> findByUserIdForUpdate(Long userId);
}

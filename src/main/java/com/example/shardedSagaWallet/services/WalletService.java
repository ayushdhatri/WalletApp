package com.example.shardedSagaWallet.services;

import com.example.shardedSagaWallet.entities.Wallet;
import com.example.shardedSagaWallet.repositories.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Wallet createWallet(Long userId){
        Wallet newWallet = Wallet.builder()
                .userId(userId)
                .isActive(true)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(newWallet);
        log.info("Wallet created successfully with id : " + newWallet.getId() + " successfully!");
        return newWallet;
    }

    public Wallet getWalletById(Long id){
        return walletRepository.findById(id).orElseThrow(() -> new RuntimeException("Wallet with id: " + id + " does not exist"));
    }

    public Wallet findWalletByUserId(Long userId){
        return walletRepository.findByIdWithLock(userId).orElseThrow(() -> new RuntimeException("No Wallet found with userId : " + userId));

    }

    @Transactional
    public void debit(Long walletId, BigDecimal amount){
        log.info("Debiting {} from wallet {}", amount, walletId);
        Wallet wallet = getWalletById(walletId);
        wallet.debit(amount);
        walletRepository.save(wallet);
        log.info("Debit Successful for wallet {}", walletId);
    }

    @Transactional
    public void credit(Long walletId, BigDecimal amount){
        log.info("Crediting {} to WalletId {}", amount, walletId);
        Wallet wallet = getWalletById(walletId);
        wallet.credit(amount);
        walletRepository.save(wallet);
        log.info("Credit Successfully for wallet {}", walletId);
    }

    public BigDecimal getWalletBalance(Long walletId){
        log.info("Getting balance for wallet {}", walletId);
        return getWalletById(walletId).getBalance();

    }





}

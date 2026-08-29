package com.example.shardedSagaWallet.services;

import com.example.shardedSagaWallet.entities.Transaction;
import com.example.shardedSagaWallet.entities.TransactionStatus;
import com.example.shardedSagaWallet.entities.TransactionType;
import com.example.shardedSagaWallet.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public Transaction getTransactionById(Long id){
        return transactionRepository.findById(id).orElseThrow(() -> new RuntimeException("Transaction with id " + id + " not found!"));
    }

    public List<Transaction> getTransactionByWalletId(Long id){
        return transactionRepository.findByWalletId(id);
    }

    public List<Transaction> getTransactionByFromWalletId(long fromWalletId){
        return this.transactionRepository.findByFromWalletId(fromWalletId);
    }

    public List<Transaction> getTransactionToWalletId(Long toWalletId){
        return this.transactionRepository.findByToWalletId(toWalletId);
    }

    @Transactional
    public Transaction createTransaction(Long fromWalletId, Long toWalletId, BigDecimal amount, Long sagaInstanceId, TransactionType transactionType){
        Transaction newTransaction = Transaction.builder().fromWalletId(fromWalletId)
                .toWalletId(toWalletId)
                .sagaInstanceId(sagaInstanceId)
                .status(TransactionStatus.PENDING)
                .type(transactionType)
                .build();
        Transaction savedTransaction = transactionRepository.save(newTransaction);
        log.info("Transaction created with id {}", savedTransaction.getId());
        return savedTransaction;
    }

    public List<Transaction> getTransactionBySagaInstanceId(Long sagaInstanceId){
        return transactionRepository.findBySagaInstanceId(sagaInstanceId);
    }







}


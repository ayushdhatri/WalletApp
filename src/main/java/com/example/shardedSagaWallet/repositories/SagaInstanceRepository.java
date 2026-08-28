package com.example.shardedSagaWallet.repositories;

import com.example.shardedSagaWallet.entities.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long> {


}

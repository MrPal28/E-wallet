package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Kyc;
@Repository
public interface KycRepository extends JpaRepository<Kyc, Long>{

  Optional<Kyc> findByUserId(Long userId);
  
}

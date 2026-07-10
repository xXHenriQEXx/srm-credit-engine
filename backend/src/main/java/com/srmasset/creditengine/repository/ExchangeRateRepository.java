package com.srmasset.creditengine.repository;

import com.srmasset.creditengine.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    @Query("""
            SELECT er FROM ExchangeRate er
            WHERE er.baseCurrency.code = :base AND er.quoteCurrency.code = :quote
            ORDER BY er.effectiveAt DESC
            LIMIT 1
            """)
    Optional<ExchangeRate> findLatest(String base, String quote);
}

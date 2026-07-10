package com.srmasset.creditengine.repository;

import com.srmasset.creditengine.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, String> {
}

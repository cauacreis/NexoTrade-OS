package com.nexotrade.os.repository;

import com.nexotrade.os.model.TradeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<TradeRecord, Long> {
}

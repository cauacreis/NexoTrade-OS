package com.nexotrade.os.repository;

import com.nexotrade.os.model.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {
}

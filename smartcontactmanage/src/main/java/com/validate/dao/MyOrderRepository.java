package com.validate.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.validate.entitie.MyOrder;

public interface MyOrderRepository extends JpaRepository<MyOrder,Long> {
   
	public MyOrder findByOrderId(String orderId);
}

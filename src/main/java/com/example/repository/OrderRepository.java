package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import com.example.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
	@Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderProducts")
	List<Order> findAllWithProducts();
}

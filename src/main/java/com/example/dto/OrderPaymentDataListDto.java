package com.example.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.form.OrderPaymentForm;

import lombok.Data;

@Component
@Data
public class OrderPaymentDataListDto {
	private List<OrderPaymentForm> orderPaymentList;

	public List<OrderPaymentForm> getOrderPaymentList() {
		return orderPaymentList;
	}

	public void setOrderPaymentList(List<OrderPaymentForm> orderPaymentList) {
		this.orderPaymentList = orderPaymentList;
	}
}

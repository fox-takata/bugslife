package com.example.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.form.OrderDeliveryForm;

import lombok.Data;

@Data
@Component
public class OrderShippingDataListDto {
    private List<OrderDeliveryForm> orderShippingList;

    public List<OrderDeliveryForm> getOrderShippingList() {
        return orderShippingList;
    }

    public void setOrderShippingList(List<OrderDeliveryForm> orderShippingList) {
        this.orderShippingList = orderShippingList;
    }
}
package com.example.model;

import java.io.Serializable;
import java.lang.String;
import java.sql.Date;
import jakarta.persistence.Transient;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_deliveries") // テーブル名を指定してください
public class OrderDelivery extends TimeEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "shipping_code")
	private String shippingCode;

	@Column(name = "shipping_date")
	private Date shippingDate;

	@Column(name = "delivery_date")
	private Date deliveryDate;

	@Column(name = "delivery_timezone")
	private String deliveryTimezone;

	@Transient
	private boolean checked;

	@Column(name = "upload_status")
	private String uploadStatus;

}
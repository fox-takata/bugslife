package com.example.form;

import java.io.Serializable;
import java.lang.String;
import java.sql.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDeliveryForm implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long orderId;

	private String shippingCode;

	private Date shippingDate;

	private Date deliveryDate;

	private String deliveryTimezone;

	// @Transient // データベースにはマッピングしない
	private boolean checked; // 選択状態を表すプロパティ

	private String uploadStatus;
}
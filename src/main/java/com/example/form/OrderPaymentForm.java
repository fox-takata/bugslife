package com.example.form;

import java.io.Serializable;
import java.lang.String;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderPaymentForm implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer orderId;
	private Double paid;
	private String method;

	// @Transient // データベースにはマッピングしない
	private boolean checked; // 選択状態を表すプロパティ

	private String uploadStatus;
}

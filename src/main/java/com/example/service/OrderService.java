package com.example.service;

import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.example.constants.TaxType;
// import com.example.dto.OrderShippingDataListDto;
import com.example.dto.OrderPaymentDataListDto;

import com.example.enums.CampaignStatus;
import com.example.enums.OrderStatus;
import com.example.enums.PaymentStatus;
import com.example.form.OrderForm;
import com.example.form.OrderPaymentForm;

import com.example.model.Campaign;
import com.example.model.Order;
import com.example.model.OrderPayment;
import com.example.model.OrderProduct;
// import com.example.model.OrderDelivery

import com.example.repository.OrderRepository;
import com.example.repository.ProductRepository;
// import com.example.repository.OrderDeliveryRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
// import java.util.Date;
import java.sql.Date;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Timestamp;

@Service
@Transactional(readOnly = true)
public class OrderService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;

	public List<Order> findAll() {
		return orderRepository.findAll();
	}

	public Optional<Order> findOne(Long id) {
		return orderRepository.findById(id);
	}

	@Transactional(readOnly = false)
	public Order save(Order entity) {
		return orderRepository.save(entity);
	}

	@Transactional(readOnly = false)
	public Order create(OrderForm.Create entity) {
		Order order = new Order();
		order.setCustomerId(entity.getCustomerId());
		order.setShipping(entity.getShipping());
		order.setNote(entity.getNote());
		order.setPaymentMethod(entity.getPaymentMethod());
		order.setStatus(OrderStatus.ORDERED);
		order.setPaymentStatus(PaymentStatus.UNPAID);
		order.setPaid(0.0);

		var orderProducts = new ArrayList<OrderProduct>();
		entity.getOrderProducts().forEach(p -> {
			var product = productRepository.findById(p.getProductId()).get();
			var orderProduct = new OrderProduct();
			orderProduct.setProductId(product.getId());
			orderProduct.setCode(product.getCode());
			orderProduct.setName(product.getName());
			orderProduct.setQuantity(p.getQuantity());
			orderProduct.setPrice((double)product.getPrice());
			orderProduct.setDiscount(p.getDiscount());
			orderProduct.setTaxType(TaxType.get(product.getTaxType()));
			orderProducts.add(orderProduct);
		});

		// 計算
		var total = 0.0;
		var totalTax = 0.0;
		var totalDiscount = 0.0;
		for (var orderProduct : orderProducts) {
			var price = orderProduct.getPrice();
			var quantity = orderProduct.getQuantity();
			var discount = orderProduct.getDiscount();
			var tax = 0.0;
			/**
			 * 税額を計算する
			 */
			if (orderProduct.getTaxIncluded()) {
				// 税込みの場合
				tax = price * quantity * orderProduct.getTaxRate() / (100 + orderProduct.getTaxRate());
			} else {
				// 税抜きの場合
				tax = price * quantity * orderProduct.getTaxRate() / 100;
			}
			// 端数処理
			tax = switch (orderProduct.getTaxRounding()) {
			case TaxType.ROUND -> Math.round(tax);
			case TaxType.CEIL -> Math.ceil(tax);
			case TaxType.FLOOR -> Math.floor(tax);
			default -> tax;
			};
			var subTotal = price * quantity + tax - discount;
			total += subTotal;
			totalTax += tax;
			totalDiscount += discount;
		}
		order.setTotal(total);
		order.setTax(totalTax);
		order.setDiscount(totalDiscount);
		order.setGrandTotal(total + order.getShipping());
		order.setOrderProducts(orderProducts);

		orderRepository.save(order);

		return order;

	}

	@Transactional()
	public void delete(Order entity) {
		orderRepository.delete(entity);
	}

	@Transactional(readOnly = false)
	public void createPayment(OrderForm.CreatePayment entity) {
		var order = orderRepository.findById(entity.getOrderId()).get();
		/**
		 * 新しい支払い情報を登録する
		 */
		var payment = new OrderPayment();
		payment.setType(entity.getType());
		payment.setPaid(entity.getPaid());
		payment.setMethod(entity.getMethod());
		payment.setPaidAt(entity.getPaidAt());

		/**
		 * 支払い情報を更新する
		 */
		// orderのorderPaymentsに追加
		order.getOrderPayments().add(payment);
		// 支払い済み金額を計算
		var paid = order.getOrderPayments().stream().mapToDouble(p -> p.getPaid()).sum();
		// 合計金額から支払いステータスを判定
		var paymentStatus = paid > order.getGrandTotal() ? PaymentStatus.OVERPAID
				: paid < order.getGrandTotal() ? PaymentStatus.PARTIALLY_PAID : PaymentStatus.PAID;

		// 更新
		order.setPaid(paid);
		order.setPaymentStatus(paymentStatus);
		orderRepository.save(order);
	}

	// CSVファイルを読み込んでOrderDeliveryFormオブジェクトのリストに変換するメソッド
	public List<OrderPaymentForm> parseCsvFilePayment(MultipartFile file) throws IOException {
		List<OrderPaymentForm> orderPaymentList = new ArrayList<>();

		try (InputStream is = file.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr)) {
			String line;
			boolean isFirstLine = true;
			int lineNumber = 1; // 行番号を保持

			while ((line = br.readLine()) != null) {
				if (isFirstLine) {
					isFirstLine = false;
					continue;
				}

				try {
					String[] fields = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
					for (int i = 0; i < fields.length; i++) {
						fields[i] = fields[i].replaceAll("^\"|\"$", ""); // ダブルクォーテーションを除去
					}

					if (fields.length < 2) {
						throw new ServiceException(lineNumber + "行目に必要なキーがありません。");
					}

					OrderPaymentForm orderPayment = new OrderPaymentForm();
					orderPayment.setOrderId(Integer.parseInt(fields[0]));
					orderPayment.setPaid(Double.parseDouble(fields[1]));
					// orderPayment.setMethod(fields[2]);
					Order order = this.findOne(Long.parseLong(fields[0])).get();
					orderPayment.setMethod(order.getPaymentMethodName());

					orderPaymentList.add(orderPayment);
					// System.out.println(orderPayment.getOrderId());

					lineNumber++;
				} catch (Exception e) {
					throw new ServiceException("予期せぬエラーが発生しました。ファイルが壊れている可能性があります。");
					// 他に必要な処理を追加することもできます（ログの記録など）。
				}
			}
		}

		return orderPaymentList;
	}

	// CSVファイルを読み込んでOrderDeliveryFormオブジェクトのリストに変換するメソッド変更
	public List<OrderPaymentForm> parseCsvFilePayment1(MultipartFile file) throws IOException {
		List<OrderPaymentForm> orderPaymentList = new ArrayList<>();

		try (InputStream is = file.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr)) {
			String line;
			boolean isFirstLine = true;
			int lineNumber = 1; // 行番号を保持

			while ((line = br.readLine()) != null) {
				if (isFirstLine) {
					isFirstLine = false;
					continue;
				}

				String[] fields = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				for (int i = 0; i < fields.length; i++) {
					fields[i] = fields[i].replaceAll("^\"|\"$", ""); // ダブルクォーテーションを除去
				}

				try {
					if (fields.length < 2) {
						throw new ServiceException(lineNumber + "行目に 必要なキーがありません。");
					}

					OrderPaymentForm orderPayment = new OrderPaymentForm();
					orderPayment.setOrderId(Integer.parseInt(fields[0]));
					orderPayment.setPaid(Double.parseDouble(fields[1]));
					// orderPayment.setMethod(fields[2]);
					Order order = this.findOne(Long.parseLong(fields[0])).get();
					orderPayment.setMethod(order.getPaymentMethodName());

					orderPaymentList.add(orderPayment);
					// System.out.println(orderPayment.getOrderId());
				} catch (DateTimeParseException | NumberFormatException e) {
					throw new ServiceException(lineNumber + "行目の 型が正しくありません。", e);
				}

				lineNumber++;
			}
		}

		return orderPaymentList;
	}

	// 受注情報を更新するメソッド
	@Transactional
	public OrderPaymentDataListDto updatePaymentInfo(OrderPaymentDataListDto orderPaymentData) {
		Double count = 0.0;
		List<OrderPaymentForm> updatedList = new ArrayList<>();

		for (OrderPaymentForm orderPaymentForm : orderPaymentData.getOrderPaymentList()) {
			// 現在の日時を取得
			Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
			if (orderPaymentForm.isChecked()) {
				// Order order = orderRepository.findById(orderPaymentForm.getOrderId())
				Order order = orderRepository.findById(Long.valueOf(orderPaymentForm.getOrderId()))
						.orElseThrow(() -> new IllegalArgumentException("該当の受注が見つかりません"));
				List<OrderPayment> orderPayments = order.getOrderPayments();

				if (order.getGrandTotal() > order.getPaid()) {
					// 更新処理を実行
					// OrderPaymentも同様に更新
					// OrderPayment orderPayment =
					// orderPaymentRepository.findByOrderId(order.getId()).get();
					OrderPayment orderPayment = new OrderPayment();
					orderPayment.setOrderId(order.getId());
					orderPayment.setMethod(order.getPaymentMethod());
					orderPayment.setPaid(orderPaymentForm.getPaid());
					orderPayment.setPaidAt(currentTimestamp);
					count = order.getPaid() + orderPaymentForm.getPaid();
					orderPayment.setType("completed");
					if (order.getGrandTotal() <= count) {
						order.setPaymentStatus("paid");
						order.setPaid(order.getGrandTotal());
						orderPayment.setPaid(orderPaymentForm.getPaid() - (count - order.getGrandTotal()));
						if (order.getStatusName().equals("発送済み")) {
							order.setStatus("completed");
						}
					} else {
						order.setPaymentStatus("partially_paid");
						order.setPaid(count);
					}
					orderPayments.add(orderPayment);
					order.setOrderPayments(orderPayments);
					orderRepository.save(order);

					orderPaymentForm.setUploadStatus("success");
				} else {
					orderPaymentForm.setUploadStatus("error");
				}

				updatedList.add(orderPaymentForm);
			} else {
				updatedList.add(orderPaymentForm);
			}
		}
		OrderPaymentDataListDto updatedListDto = new OrderPaymentDataListDto();
		updatedListDto.setOrderPaymentList(updatedList);

		return updatedListDto;
	}
}

package com.example.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.nio.file.Path;

import com.example.constants.Message;
import com.example.enums.OrderStatus;
import com.example.enums.PaymentMethod;
import com.example.enums.PaymentStatus;
import com.example.form.OrderForm;
import com.example.form.OrderPaymentForm;
import com.example.dto.OrderPaymentDataListDto;

import com.example.model.Order;
import com.example.service.OrderService;
import com.example.service.ProductService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/orders")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private ProductService productService;

	@Autowired
	private OrderPaymentDataListDto orderPaymentListDto;

	@ModelAttribute
	OrderPaymentDataListDto setFormDto() {
		return new OrderPaymentDataListDto();
	}

	@GetMapping
	public String index(Model model) {
		List<Order> all = orderService.findAll();
		model.addAttribute("listOrder", all);
		return "order/index";
	}

	@GetMapping("/{id}")
	public String show(Model model, @PathVariable("id") Long id) {
		if (id != null) {
			Optional<Order> order = orderService.findOne(id);
			model.addAttribute("order", order.get());
		}
		return "order/show";
	}

	@GetMapping(value = "/new")
	public String create(Model model, @ModelAttribute OrderForm.Create entity) {
		model.addAttribute("order", entity);
		model.addAttribute("products", productService.findAll());
		model.addAttribute("paymentMethods", PaymentMethod.values());
		return "order/create";
	}

	@PostMapping
	public String create(@Validated @ModelAttribute OrderForm.Create entity, BindingResult result,
			RedirectAttributes redirectAttributes) {
		Order order = null;
		try {
			order = orderService.create(entity);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_INSERT);
			return "redirect:/orders/" + order.getId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/orders";
		}
	}

	@GetMapping("/{id}/edit")
	public String update(Model model, @PathVariable("id") Long id) {
		try {
			if (id != null) {
				Optional<Order> entity = orderService.findOne(id);
				model.addAttribute("order", entity.get());
				model.addAttribute("paymentMethods", PaymentMethod.values());
				model.addAttribute("paymentStatus", PaymentStatus.values());
				model.addAttribute("orderStatus", OrderStatus.values());
			}
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return "order/form";
	}

	@PutMapping
	public String update(@Validated @ModelAttribute Order entity, BindingResult result,
			RedirectAttributes redirectAttributes) {
		Order order = null;
		try {
			order = orderService.save(entity);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_UPDATE);
			return "redirect:/orders/" + order.getId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/orders";
		}
	}

	@DeleteMapping("/{id}")
	public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		try {
			if (id != null) {
				Optional<Order> entity = orderService.findOne(id);
				orderService.delete(entity.get());
				redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_DELETE);
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			throw new ServiceException(e.getMessage());
		}
		return "redirect:/orders";
	}

	@PostMapping("/{id}/payments")
	public String createPayment(@Validated @ModelAttribute OrderForm.CreatePayment entity, BindingResult result,
			RedirectAttributes redirectAttributes) {
		try {
			orderService.createPayment(entity);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_PAYMENT_INSERT);
			return "redirect:/orders/" + entity.getOrderId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/orders";
		}
	}

	@GetMapping("/payment")
	public String showOrderPaymentPage(Model model) {
		// ここで必要なデータをモデルに追加する処理を行う
		// 例えば、CSVの読み込み結果やバリデーションエラーなどのデータをモデルに追加する

		// 仮のデータをモデルに追加する例（実際のデータを追加してください）
		// model.addAttribute("validationError", null); // バリデーションエラー
		model.addAttribute("orderPaymentData", null); // CSVの読み込み内容
		return "order/payment"; // Thymeleafテンプレートの名前を返す
	}

	/**
	 * CSVインポート処理
	 *
	 * @param uploadFile
	 * @param redirectAttributes
	 * @return
	 */
	@PostMapping("/payment/upload")
	public String uploadFile(@RequestParam("file") MultipartFile uploadFile, RedirectAttributes redirectAttributes,
			Model model) {
		if (uploadFile.isEmpty()) {
			// ファイルが存在しない場合
			redirectAttributes.addFlashAttribute("error", "ファイルを選択してください。");
			return "redirect:/orders/payment"; // 適切なリダイレクト先に修正する
		}
		if (!"text/csv".equals(uploadFile.getContentType())) {
			// CSVファイル以外の場合
			redirectAttributes.addFlashAttribute("error", "CSVファイルを選択してください。");
			return "redirect:/orders/payment"; // 適切なリダイレクト先に修正する
		}
		try {
			List<OrderPaymentForm> orderPaymentList = orderService.parseCsvFilePayment(uploadFile);// インポート処理を行うメソッドを呼び出す
			OrderPaymentDataListDto orderPaymentListDto = new OrderPaymentDataListDto(); // この行を追加

			orderPaymentListDto.setOrderPaymentList(orderPaymentList);

			model.addAttribute("orderPaymentData", orderPaymentListDto);
			// redirectAttributes.addFlashAttribute("orderShippingList", orderShippingList);
			// redirectAttributes.addFlashAttribute("success", "CSVファイルのインポートに成功しました。");
			model.addAttribute("success", "CSVファイルのインポートに成功しました。");
			return "order/payment"; // 適切なリダイレクト先に修正する
			// return "redirect:/orders/shipping";
		} catch (Throwable e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			e.printStackTrace();
			return "redirect:/orders/payment"; // 適切なリダイレクト先に修正する
		}

		// return "redirect:/orders/shipping"; // 適切なリダイレクト先に修正する

	}

	// 入金情報の更新
	@PutMapping("/payment/update")
	public String updatePaymentInfo(@ModelAttribute("orderPaymentData") OrderPaymentDataListDto orderPaymentData,
			Model model) {
		OrderPaymentDataListDto updatedListDto = orderService.updatePaymentInfo(orderPaymentData);
		model.addAttribute("orderPaymentData", updatedListDto);
		model.addAttribute("success", "出荷情報を更新しました。");

		return "order/payment";
	}

	@PostMapping("/payment/download")
	public void download(HttpServletResponse response) {
		try (OutputStream os = response.getOutputStream();) {
			Path filePath = new ClassPathResource("static/templates/order_payment_data.csv").getFile().toPath();
			byte[] templateData = Files.readAllBytes(filePath);
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
			String formattedDateTime = now.format(formatter);
			String attachment = "attachment; filename=order_payment_" +
					formattedDateTime + ".csv";

			response.setContentType("text/csv");
			response.setHeader("Content-Disposition", attachment);
			response.setContentLength(templateData.length);
			os.write(templateData);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

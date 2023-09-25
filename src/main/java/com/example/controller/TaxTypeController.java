package com.example.controller;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.constants.Message;
import com.example.form.CampaignForm;
import com.example.model.TaxType;
import com.example.service.TaxTypeService;
import com.example.service.ProductService;
import com.example.utils.CheckUtil;

@Controller
@RequestMapping("/taxType")
public class TaxTypeController {

	@Autowired
	private TaxTypeService taxTypeService;

	@Autowired
	private ProductService productService;

	private String regex = "^(100|[1-9][0-9]?|0)$";

	/**
	 * 一覧画面表示
	 *
	 * @param model
	 * @param form
	 * @return
	 */
	@GetMapping
	public String index(Model model, @ModelAttribute("form") CampaignForm form) {
		List<TaxType> all = taxTypeService.findAll();
		model.addAttribute("listTax", all);
		model.addAttribute("form", form);
		return "tax_type/index";
	}

	// 詳細表示画面
	@GetMapping("/{id}")
	public String show(Model model, @PathVariable("id") Long id) {
		if (id != null) {
			Optional<TaxType> tax = taxTypeService.findOne(id);
			model.addAttribute("tax", tax.get());
		}
		return "tax_type/show";
	}

	// 新規登録画面
	@GetMapping(value = "/new")
	public String create(Model model, @ModelAttribute TaxType entity) {
		model.addAttribute("tax", entity);
		return "tax_type/form";
	}

	// 新規作成メソッド
	@PostMapping
	public String create(@Validated @ModelAttribute TaxType entity, BindingResult result,
			RedirectAttributes redirectAttributes) {
		TaxType tax = null;
		try {
			// dnameはDescripdin同様2000文字まで
			if (!CheckUtil.checkDescriptionLength(entity.getName())) {
				// NG
				redirectAttributes.addFlashAttribute("error", Message.MSG_VALIDATE_ERROR);
				return "redirect:/taxType";
			} else if (Integer.toString(entity.getRate()).isEmpty()
					|| Pattern.matches(this.regex, Integer.toString(entity.getRate()))) {
				tax = taxTypeService.save(entity);
				redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_INSERT);
				return "redirect:/taxType/" + tax.getId();
			} else {
				// NG
				redirectAttributes.addFlashAttribute("error", Message.MSG_VALIDATE_ERROR);
				return "redirect:/taxType";
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/taxType";
		}
	}

	// 編集画面
	@GetMapping("/{id}/edit")
	public String update(Model model, @PathVariable("id") Long id) {
		try {
			if (id != null) {
				Optional<TaxType> entity = taxTypeService.findOne(id);
				model.addAttribute("tax", entity.get());
			}
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return "tax_type/form";
	}

	// 更新処理メソッド
	@PutMapping
	public String update(@Validated @ModelAttribute TaxType entity, BindingResult result,
			RedirectAttributes redirectAttributes) {

		Boolean isCheck = true;
		try {
			int intValue = entity.getId().intValue(); // Long型をint型に変換
			// intValue を使用する
			isCheck = productService.isTaxType(intValue);
		} catch (ArithmeticException e) {
			// 変換に失敗した場合の処理
			redirectAttributes.addFlashAttribute("error", "Long型の値がint型の範囲外です。");
			return "redirect:/taxType";
		}

		TaxType tax = null;
		try {
			// nameはDescripdin同様2000文字まで
			if (!CheckUtil.checkDescriptionLength(entity.getName())) {
				// NG
				redirectAttributes.addFlashAttribute("error", Message.MSG_VALIDATE_ERROR);
				return "redirect:/taxType";
			} else if (Integer.toString(entity.getRate()).isEmpty()
					|| Pattern.matches(regex, Integer.toString(entity.getRate()))) {
				if (isCheck) {
					// 編集したIDが使われていない場合
					tax = taxTypeService.save(entity);
					redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_UPDATE);
					return "redirect:/taxType/" + tax.getId();
				} else {
					// 編集したIDが使われていた場合
					redirectAttributes.addFlashAttribute("error", "使用中のIDの編集はできません");
					return "redirect:/taxType";
				}
			} else {
				redirectAttributes.addFlashAttribute("error", Message.MSG_VALIDATE_ERROR);
				return "redirect:/taxType";
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/taxType";
		}
	}

	// 削除処理
	@DeleteMapping("/{id}")
	public String delete(@PathVariable("id") Long id,
			RedirectAttributes redirectAttributes) {
		Boolean isCheck = true;
		try {
			int intValue = id.intValue(); // Long型をint型に変換
			// intValue を使用する
			isCheck = productService.isTaxType(intValue);
		} catch (ArithmeticException e) {
			// 変換に失敗した場合の処理
			redirectAttributes.addFlashAttribute("error", "Long型の値がint型の範囲外です。");
			return "redirect:/taxType";
		}

		try {
			if (id != null && isCheck) {
				Optional<TaxType> entity = taxTypeService.findOne(id);
				taxTypeService.delete(entity.get());
				redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_DELETE);
			} else {
				redirectAttributes.addFlashAttribute("error", "使用中のIDの削除はできません");
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			// throw new ServiceException(e.getMessage());
		}
		return "redirect:/taxType";
	}

}

package com.example.controller;

import java.util.List;
import java.util.regex.Pattern;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.constants.Message;
import com.example.form.CampaignForm;
import com.example.model.TaxType;
import com.example.service.TaxTypeService;
import com.example.service.ProductService;

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
		List<Integer> rateAll = taxTypeService.findAllRates();
		model.addAttribute("rateAll", rateAll);
		model.addAttribute("form", form);
		return "tax_type/index";
	}

	// 詳細表示画面
	@GetMapping("/{rate}")
	public String show(Model model, @PathVariable("rate") Integer rate) {
		if (rate != null) {
			List<TaxType> byRate = taxTypeService.findByRate(rate);
			model.addAttribute("taxs", byRate);
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
		try {
			// バリデーションチェック
			if (Integer.toString(entity.getRate()).isEmpty()
					|| Pattern.matches(this.regex, Integer.toString(entity.getRate()))) {
				taxTypeService.saveAllCombinations(entity);
				redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_INSERT);
				return "redirect:/taxType/" + entity.getRate();
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

	// 削除処理
	@DeleteMapping("/{rate}")
	public String delete(@PathVariable("rate") Integer rate,
			RedirectAttributes redirectAttributes) {
		Boolean isCheck = false;
		List<TaxType> byRate = taxTypeService.findByRate(rate);
		// byRate を使用して、使用していないか判断する
		isCheck = productService.isTaxTypeList(byRate);

		try {
			if (rate != null && isCheck) {
				taxTypeService.deletes(byRate);
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

package com.example.form;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

// import com.example.constants.TaxType;
import com.example.model.CategoryProduct;
import com.example.model.Product;
import com.example.model.TaxType;
import com.example.service.TaxTypeService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ProductForm {

	@Autowired
	private TaxTypeService taxTypeService;

	private Long id;

	@NotNull
	private Long shopId;

	@NotBlank(message = "商品名を入力してください。")
	@Size(max = 255, message = "商品名は255文字以内で入力してください。")
	private String name;

	@NotBlank(message = "商品コードを入力してください。")
	@Size(max = 255, message = "商品コードは255文字以内で入力してください。")
	private String code;

	@Valid
	private List<Long> categoryIds = new ArrayList<Long>();

	@NotNull(message = "重さを入力してください。")
	private Integer weight;

	@NotNull(message = "高さを入力してください。")
	private Integer height;

	@NotNull(message = "値段を入力してください。")
	private Double price;

	@NotNull(message = "税率を選択してください。")
	private Integer rate = 10;

	@NotNull(message = "入力価格を選択してください。")
	private Boolean taxIncluded = false;

	@NotNull(message = "端数処理を選択してください。")
	private String rounding = "floor";

	public ProductForm(Product product, TaxTypeService taxTypeService) {
		this.setId(product.getId());
		this.setShopId(product.getShopId());
		this.setName(product.getName());
		this.setCode(product.getCode());
		// 紐づくカテゴリIDのリストを作成
		List<CategoryProduct> categoryProducts = product.getCategoryProducts();
		if (categoryProducts != null) {
			List<Long> categoryIds = categoryProducts.stream().map(categoryProduct -> categoryProduct.getCategoryId())
					.collect(Collectors.toList());
			this.setCategoryIds(categoryIds);
		}
		this.setWeight(product.getWeight());
		this.setHeight(product.getHeight());
		this.setPrice(product.getPrice());
		this.taxTypeService = taxTypeService;
		TaxType tax = taxTypeService.findOne(product.getTaxType().longValue()).get();

		this.setRate(tax.getRate());
		this.setTaxIncluded(tax.getTaxIncluded());
		this.setRounding(tax.getRounding());
	}

	public Integer getTaxType(TaxTypeService taxTypeService) {
		this.taxTypeService = taxTypeService;
		Long taxId = taxTypeService.findIdRateIncRound(rate, taxIncluded, rounding);
		return taxId.intValue();
	}
}

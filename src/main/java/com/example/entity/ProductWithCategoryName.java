package com.example.entity;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductWithCategoryName {

	private Long id;

	private String name;

	private String code;

	private Integer weight;

	private Integer height;

	private Integer price;

	private List<String> categoryNames;

	public ProductWithCategoryName(Long id, String code, String name, Integer weight, Integer height, Integer price,
			List<String> categoryNames) {
		this.setId(id);
		this.setCode(code);
		this.setName(name);
		this.setWeight(weight);
		this.setHeight(height);
		this.setPrice(price);
		this.setCategoryNames(categoryNames);
	}
}

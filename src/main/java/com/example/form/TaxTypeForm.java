package com.example.form;

// import com.example.constants.TaxType;
import com.example.model.TaxType;

// import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class TaxTypeForm {

	private Long id;

	@NotBlank(message = "名称を入力してください。")
	@Size(max = 2000, message = "名前は2000文字以内で入力してください。")
	private String name;

	@NotNull(message = "税率を選択してください。")
	private Integer rate;

	@NotNull(message = "入力価格を選択してください。")
	private Boolean taxIncluded = false;

	@NotNull(message = "端数処理を選択してください。")
	private String rounding;

	public TaxTypeForm(TaxType taxType) {
		this.setId(taxType.getId());
		this.setName(taxType.getName());
		this.setRate(taxType.getRate());
		this.setTaxIncluded(taxType.getTaxIncluded());
		this.setRounding(taxType.getRounding());
	}

	// public Integer getTaxType() {
	// var tax = TaxType.get(rate, taxIncluded, rounding);
	// return tax.id;
	// }
}

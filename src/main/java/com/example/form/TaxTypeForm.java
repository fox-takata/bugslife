package com.example.form;

import org.springframework.beans.factory.annotation.Autowired;

// import com.example.constants.TaxType;
import com.example.model.TaxType;
import com.example.service.TaxTypeService;

// import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class TaxTypeForm {

	@Autowired
	private TaxTypeService taxTypeService;

	private Long id;

	@NotNull(message = "税率を選択してください。")
	private Integer rate;

	@NotNull(message = "入力価格を選択してください。")
	private Boolean taxIncluded = false;

	@NotNull(message = "端数処理を選択してください。")
	private String rounding;

	public TaxTypeForm(TaxType taxType) {
		this.setId(taxType.getId());
		this.setRate(taxType.getRate());
		this.setTaxIncluded(taxType.getTaxIncluded());
		this.setRounding(taxType.getRounding());
	}

	public Integer getTaxType() {
		Long tax = taxTypeService.findIdRateIncRound(rate, taxIncluded, rounding);
		return tax.intValue();
	}
}

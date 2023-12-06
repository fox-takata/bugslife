package com.example.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tax_types")
public class TaxType extends TimeEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "rate", nullable = false)
	private Integer rate;

	@Column(name = "tax_included", nullable = false)
	private Boolean taxIncluded;

	@Column(name = "rounding", nullable = false)
	private String rounding;

	public String getRoundName() {
		if ("floor".equals(this.rounding)) {
			return "切り捨て";
		} else if ("ceil".equals(this.rounding)) {
			return "切り上げ";
		} else {
			return "四捨五入";
		}
	}

	public String getIncudedName() {
		return this.taxIncluded ? "税込み" : "税抜き";
	}
}

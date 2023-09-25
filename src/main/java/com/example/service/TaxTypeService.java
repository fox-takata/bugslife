package com.example.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.TaxType;
import com.example.repository.TaxTypeRepository;

@Service
public class TaxTypeService {

	@Autowired
	private TaxTypeRepository taxTypeRepository;

	public List<TaxType> findAll() {
		return taxTypeRepository.findAll();
	}

	public Optional<TaxType> findOne(Long id) {
		return taxTypeRepository.findById(id);
	}

	public TaxType save(TaxType taxType) {
		String name = "税率：" + taxType.getRate() + "% , 入力価格：" + taxType.getIncudedName() + " , 端数処理："
				+ taxType.getRoundName();
		taxType.setName(name);
		return taxTypeRepository.save(taxType);
	}

	public void delete(TaxType taxType) {
		taxTypeRepository.delete(taxType);
	}

	public Long findIdRateIncRound(Integer rate, Boolean taxIncluded, String rounding) {
		System.out.println("結果" + taxTypeRepository.findIdByRateAndTaxIncludedAndRounding(rate, taxIncluded, rounding));
		return taxTypeRepository.findIdByRateAndTaxIncludedAndRounding(rate, taxIncluded, rounding);
	}

}

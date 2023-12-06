package com.example.service;

import java.util.ArrayList;
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

	// TaxRepositoryからrateの一覧を取得するメソッド
	public List<Integer> findAllRates() {
		return taxTypeRepository.findAllRates();
	}

	public List<TaxType> findByRate(Integer rate) {
		return taxTypeRepository.findByRate(rate);
	}

	public Optional<TaxType> findOne(Long id) {
		return taxTypeRepository.findById(id);
	}

	public void saveAllCombinations(TaxType taxType) {
		List<TaxType> taxTypeList = new ArrayList<>();

		String[] roundings = { "floor", "round", "ceil" };
		Boolean[] taxIncludeds = { false, true };

		for (Boolean taxIncluded : taxIncludeds) {
			for (String rounding : roundings) {
				TaxType newTaxType = new TaxType();
				newTaxType.setRate(taxType.getRate());
				newTaxType.setRounding(rounding);
				newTaxType.setTaxIncluded(taxIncluded);
				taxTypeList.add(newTaxType);
			}
		}
		taxTypeRepository.saveAll(taxTypeList);
	}

	public void deletes(List<TaxType> taxTypes) {
		taxTypeRepository.deleteAll(taxTypes);
	}

	public Long findIdRateIncRound(Integer rate, Boolean taxIncluded, String rounding) {
		return taxTypeRepository.findIdByRateAndTaxIncludedAndRounding(rate, taxIncluded, rounding);
	}

}

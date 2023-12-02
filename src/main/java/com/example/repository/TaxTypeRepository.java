package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.model.TaxType;

@Repository
public interface TaxTypeRepository extends JpaRepository<TaxType, Long> {

	@Query("SELECT t.id FROM TaxType t WHERE t.rate = :rate AND t.taxIncluded = :taxIncluded AND t.rounding = :rounding")
	Long findIdByRateAndTaxIncludedAndRounding(@Param("rate") Integer rate, @Param("taxIncluded") Boolean taxIncluded,
			@Param("rounding") String rounding);

	// 税率で検索かけた結果
	List<TaxType> findByRate(Integer rate);

	// rateの一覧を取得するメソッド
	@Query("SELECT DISTINCT t.rate FROM TaxType t")
	List<Integer> findAllRates();
}

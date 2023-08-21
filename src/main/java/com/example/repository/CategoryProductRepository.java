package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.model.CategoryProduct;
import org.springframework.data.jpa.repository.Query;

public interface CategoryProductRepository extends JpaRepository<CategoryProduct, Long> {
	void deleteByCategoryId(Long categoryId);

	// productIdで検索
	List<CategoryProduct> findByProductId(Long productId);

	List<CategoryProduct> findByCategoryId(Long categoryId);

	// カテゴリIDで紐づく商品の名前のリストを返却
	@Query("SELECT p.product.name FROM CategoryProduct p WHERE p.categoryId = ?1")
	List<String> findProductNamesByCategoryId(Long categoryId);

}

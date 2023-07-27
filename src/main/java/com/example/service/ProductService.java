package com.example.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.Category;
import com.example.model.CategoryProduct;
import com.example.model.Product;
import com.example.repository.CategoryProductRepository;
import com.example.repository.ProductRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

import com.example.entity.ProductWithCategoryName;
import com.example.form.ProductForm;
import com.example.form.ProductSearchForm;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ProductService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryProductRepository categoryProductRepository;

	public List<Product> findAll() {
		return productRepository.findAll();
	}

	public Optional<Product> findOne(Long id) {
		return productRepository.findById(id);
	}

	@Transactional(readOnly = false)
	public Product save(Product entity) {
		return productRepository.save(entity);
	}

	@Transactional(readOnly = false)
	public void delete(Product entity) {
		productRepository.delete(entity);
	}

	// 指定された検索条件に一致するエンティティを検索する
	public List<ProductWithCategoryName> search(Long shopId, ProductSearchForm form) {
		final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);
		final Root<Product> root = query.from(Product.class);

		Join<Product, CategoryProduct> categoryProductJoin = root.joinList("categoryProducts", JoinType.LEFT);
		Join<CategoryProduct, Category> categoryJoin = categoryProductJoin.join("category", JoinType.LEFT);

		query.multiselect(
				root.get("id"),
				root.get("code"),
				root.get("name"),
				root.get("weight"),
				root.get("height"),
				root.get("price"),
				categoryJoin.get("name").alias("categoryName")).where(builder.equal(root.get("shopId"), shopId));

		// formの値を元に検索条件を設定する
		if (!StringUtils.isEmpty(form.getName())) {
			// name で部分一致検索 (大文字・小文字を区別せず)
			query.where(builder.like(builder.upper(root.get("name")), "%" +
					form.getName().toUpperCase() + "%"));
		}

		if (!StringUtils.isEmpty(form.getCode())) {
			// code で部分一致検索 (大文字・小文字を区別せず)
			query.where(builder.like(builder.upper(root.get("code")), "%" +
					form.getCode().toUpperCase() + "%"));
		}

		if (form.getCategories() != null && form.getCategories().size() > 0) {
			// categories で完全一致検索
			query.where(categoryJoin.get("id").in(form.getCategories()));
		}

		// height で範囲検索（1つまたは2つの値が指定されている場合）
		if (form.getHeight1() != null && form.getHeight2() != null) {
			query.where(builder.between(root.get("height"), form.getHeight1(),
					form.getHeight2()));
		} else if (form.getHeight1() != null) {
			query.where(builder.greaterThanOrEqualTo(root.get("height"),
					form.getHeight1()));
		} else if (form.getHeight2() != null) {
			query.where(builder.lessThanOrEqualTo(root.get("height"),
					form.getHeight2()));
		}

		// weight で範囲検索（1つまたは2つの値が指定されている場合）
		if (form.getWeight1() != null && form.getWeight2() != null) {
			query.where(builder.between(root.get("weight"), form.getWeight1(),
					form.getWeight2()));
		} else if (form.getWeight1() != null) {
			// If weight1 is provided but weight2 is empty, filter products with weights
			// greater than or equal to weight1
			query.where(builder.greaterThanOrEqualTo(root.get("weight"),
					form.getWeight1()));
		} else if (form.getWeight2() != null) {
			query.where(builder.lessThanOrEqualTo(root.get("weight"),
					form.getWeight2()));
		}

		// price で範囲検索
		if (form.getPrice1() != null && form.getPrice2() != null) {
			query.where(builder.between(root.get("price"), form.getPrice1(),
					form.getPrice2()));
		} else if (form.getPrice1() != null) {
			query.where(builder.greaterThanOrEqualTo(root.get("price"),
					form.getPrice1()));
		} else if (form.getPrice2() != null) {
			query.where(builder.lessThanOrEqualTo(root.get("price"), form.getPrice2()));
		}

		List<Object[]> results = entityManager.createQuery(query).getResultList();

		// Map を使用して同じIDの商品を一時的に保持する
		Map<Long, ProductWithCategoryName> productsMap = new HashMap<>();

		// 検索結果を1行ずつ処理する
		for (Object[] result : results) {
			Long productId = (Long)result[0];
			String categoryName = (String)result[6];
			if (categoryName == null) {
				categoryName = ""; // カテゴリ名が空の場合、空の文字列を代わりに追加する
			}

			if (productsMap.containsKey(productId)) {
				// 既に productsMap に同じIDの商品が存在する場合、その商品の categoryNames リストに新しいカテゴリー名を追加する
				productsMap.get(productId).getCategoryNames().add(categoryName);
			} else {
				// productsMap に同じIDの商品が存在しない場合、新しい ProductWithCategoryName オブジェクトを作成し、そのオブジェクトを
				// productsMap に追加する
				List<String> categoryNames = new ArrayList<>();
				categoryNames.add(categoryName);
				ProductWithCategoryName product = new ProductWithCategoryName(
						(Long)result[0],
						(String)result[1],
						(String)result[2],
						(Integer)result[3],
						(Integer)result[4],
						(Double)result[5],
						categoryNames);
				productsMap.put(productId, product);
			}
		}
		// Map の値（ProductWithCategoryName オブジェクト）をリストに変換して返す
		return new ArrayList<>(productsMap.values());
	}

	/**
	 * ProductFormの内容を元に商品情報を保存する
	 * 
	 * @param entity
	 * @return
	 */
	@Transactional(readOnly = false)
	public Product save(ProductForm entity) {
		// 紐づくカテゴリを事前に取得
		List<CategoryProduct> categoryProducts = entity.getId() != null
				? categoryProductRepository.findByProductId(entity.getId())
				: new ArrayList<>();

		Product product = new Product(entity);
		productRepository.save(product);

		// 未処理のカテゴリーIDのリスト
		List<Long> categoryIds = entity.getCategoryIds();
		// カテゴリの紐付け解除
		for (CategoryProduct categoryProduct : categoryProducts) {
			// 紐づくカテゴリーIDが更新後のカテゴリーIDに含まれていない場合は削除
			if (!categoryIds.contains(categoryProduct.getCategoryId())) {
				categoryProductRepository.delete(categoryProduct);
			}
			// 処理が終わったものをリストから削除
			categoryIds.remove(categoryProduct.getCategoryId());
		}
		// カテゴリの紐付け登録
		for (Long categoryId : categoryIds) {
			categoryProductRepository.save(new CategoryProduct(categoryId, product.getId()));
		}

		return product;
	}
}

package com.spunit.performance.repository;

import com.spunit.performance.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * Product Repository with query optimization
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by ID with query hints for caching
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<Product> findById(Long id);

    /**
     * Find products by category with pagination
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * Find products by name containing
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<Product> searchByName(@Param("name") String name);

    /**
     * Find products by price range
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    /**
     * Find top products by category (optimized with limit)
     */
    @Query(value = "SELECT * FROM products WHERE category = :category ORDER BY price DESC LIMIT :limit",
           nativeQuery = true)
    List<Product> findTopProductsByCategory(@Param("category") String category, @Param("limit") int limit);

    /**
     * Count products by category (optimized)
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Long countByCategory(@Param("category") String category);

    /**
     * Find products with stock greater than
     */
    @Query("SELECT p FROM Product p WHERE p.stock > :minStock")
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<Product> findProductsInStock(@Param("minStock") Integer minStock);
}


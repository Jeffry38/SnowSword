package com.leixiao.snowsword.dao;

import com.leixiao.snowsword.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}

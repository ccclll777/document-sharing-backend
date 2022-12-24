package org.ccclll777.alldocsbackend.dao;

import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.ccclll777.alldocsbackend.entity.Category;

import java.util.List;

@Mapper
public interface CategoryDao {

    int insertCategory(Category category);

    int updateCategory(Category category);

    int deleteCategory(Integer categoryId);

    List<Category> selectCategoryList(Integer limit, Integer offset);

    int haveCategory(String categoryName);

    int categoryCount();
}

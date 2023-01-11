package org.ccclll777.alldocsbackend.dao;

import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.ccclll777.alldocsbackend.entity.Category;

import java.util.List;

@Mapper
public interface CategoryDao {
    /**
     * 插入分类
     * @param category
     * @return
     */
    int insertCategory(Category category);

    /**
     * 更新分类信息
     * @param category
     * @return
     */
    int updateCategory(Category category);

    /**
     * 删除分类
     * @param categoryId
     * @return
     */
    int deleteCategory(Integer categoryId);

    /**
     * 查询分类列表
     * @param limit
     * @param offset
     * @return
     */
    List<Category> selectCategoryList(Integer limit, Integer offset);

    /**
     * 是否存在这个分类
     * @param categoryName
     * @return
     */

    int haveCategory(String categoryName);

    /**
     * 分类数量
     * @return
     */
    int categoryCount();

    /**
     * 查询分类详细信息
     * @param categoryId
     * @return
     */
    Category selectCategoryById(Integer categoryId);
}

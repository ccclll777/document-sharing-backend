package org.ccclll777.alldocsbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.dao.CategoryDao;
import org.ccclll777.alldocsbackend.dao.TagDao;
import org.ccclll777.alldocsbackend.entity.Category;
import org.ccclll777.alldocsbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CategoryService {
    @Autowired
    private CategoryDao categoryDao;

    public int insertCategory(Category category) {
        int count = categoryDao.haveCategory(category.getName());
        if(count > 0) {
            return -1;
        }
        return categoryDao.insertCategory(category);
    }
    public int updateCategory(Category category){
        return categoryDao.updateCategory(category);
    }
    public int deleteCategory(int categoryId) {
        return categoryDao.deleteCategory(categoryId);
    }
    public int deleteCategoryList(String categoryIds) {
        String[] idxs = categoryIds.split(",");
        if(idxs.length == 0){
            return -1;
        }
        for (String idx : idxs) {
            Integer id = Integer.parseInt(idx);
            categoryDao.deleteCategory(id);
        }
        return 1;
    }

    public List<Category> selectCategoryList(int pageNum, int pageSize) {
        int offset = pageNum * pageSize;
        return categoryDao.selectCategoryList(pageSize,offset);
    }
    public int getCategoryCount() {
        return categoryDao.categoryCount();
    }
}

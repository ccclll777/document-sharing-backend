package org.ccclll777.alldocsbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.dao.CategoryDao;
import org.ccclll777.alldocsbackend.dao.FilesDao;
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
    @Autowired
    private FilesDao filesDao;

    /**
     * 插入分类
     * @param category
     * @return
     */
    public int insertCategory(Category category) {
        int count = categoryDao.haveCategory(category.getName());
        if(count > 0) {
            return -1;
        }
        return categoryDao.insertCategory(category);
    }

    /**
     * 更新分类
     * @param category
     * @return
     */
    public int updateCategory(Category category){
        return categoryDao.updateCategory(category);
    }

    /**
     * 删除分类
     * @param categoryId
     * @return
     */
    public int deleteCategory(int categoryId) {
        int count = filesDao.fileCountByCategoryId(categoryId);
        if (count > 0) {
            return -2;
        }
        return categoryDao.deleteCategory(categoryId);
    }

    /**
     * 批量删除分类列表
     * @param categoryIds
     * @return
     */
    public int deleteCategoryList(String categoryIds) {
        String[] idxs = categoryIds.split(",");
        if(idxs.length == 0){
            return -1;
        }
        for (String idx : idxs) {
            Integer id = Integer.parseInt(idx);
            int count = filesDao.fileCountByCategoryId(id);
            if (count <= 0) {
                categoryDao.deleteCategory(id);
            }
        }
        return 1;
    }

    /**
     * 选择分类裂帛奥
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<Category> selectCategoryList(int pageNum, int pageSize) {
        int offset = pageNum * pageSize;
        return categoryDao.selectCategoryList(pageSize,offset);
    }

    /**
     * 获取分类数量
     * @return
     */
    public int getCategoryCount() {
        return categoryDao.categoryCount();
    }
}

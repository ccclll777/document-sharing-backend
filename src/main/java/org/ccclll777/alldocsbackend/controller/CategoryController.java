package org.ccclll777.alldocsbackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.entity.Category;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.entity.dto.CategoryDTO;
import org.ccclll777.alldocsbackend.enums.ErrorCode;
import org.ccclll777.alldocsbackend.security.common.constants.SecurityConstants;
import org.ccclll777.alldocsbackend.security.common.utils.JwtTokenUtils;
import org.ccclll777.alldocsbackend.service.CategoryService;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.ccclll777.alldocsbackend.utils.RegexConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/category")
@Api(tags = "文档分类模块")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @ApiOperation(value = "新增单个分类")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")
    @PostMapping(value = "/insert")
    public BaseApiResult insert(@RequestBody CategoryDTO categoryDTO, @RequestHeader(SecurityConstants.TOKEN_HEADER) String token) {
        // 插入进来的参数必需经过清洗
        String name = categoryDTO.getName();
        if (!name.matches(RegexConstant.CH_ENG_WORD)) {
            return BaseApiResult.error(ErrorCode.PARAMS_CONTENT_ERROR.getCode(), ErrorCode.PARAMS_CONTENT_ERROR.getMessage());
        }
        //需要根据token找到userId
        String tokenValue = token.replace(SecurityConstants.TOKEN_PREFIX, "");
        String userId =   JwtTokenUtils.getId(tokenValue);
        Category category = Category.builder().name(categoryDTO.getName()).description(categoryDTO.getDescription()).userId(Integer.parseInt(userId)).build();
        int code = categoryService.insertCategory(category);
        if(code > 0){
            return  BaseApiResult.success("插入分类成功");
        } else if (code == -1) {
            return  BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "重复分类");
        }
        return  BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "插入分类失败");
    }

    @ApiOperation(value = "更新分类")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")
    @PostMapping(value = "/update")
    public BaseApiResult update(@RequestBody CategoryDTO categoryDTO) {
        // 插入进来的参数必需经过清洗
        String name = categoryDTO.getName();
        if (!name.matches(RegexConstant.CH_ENG_WORD)) {
            return BaseApiResult.error(ErrorCode.PARAMS_CONTENT_ERROR.getCode(), ErrorCode.PARAMS_CONTENT_ERROR.getMessage());
        }
        Category category = Category.builder().id(categoryDTO.getId()).name(categoryDTO.getName()).description(categoryDTO.getDescription()).build();
        int code = categoryService.updateCategory(category);
        if(code > 0){
            return  BaseApiResult.success("更新分类成功");
        }
        return  BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "更新分类失败");
    }
    @ApiOperation(value = "根据id移除某个分类")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
    @DeleteMapping(value = "/delete/{categoryId}")
    public BaseApiResult remove(@PathVariable Integer categoryId) {
        int code = categoryService.deleteCategory(categoryId);
        if(code > 0){
            return  BaseApiResult.success("删除分类成功");
        } else if (code == -2) {
            BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "删除分类失败,还有属于此分类的文档");
        }
        return  BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "删除分类失败");
    }
    @ApiOperation(value = "批量删除分类")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
    @DeleteMapping(value = "/deleteList/{categoryIds}")
    public BaseApiResult removeList(@PathVariable String categoryIds) {
        int code = categoryService.deleteCategoryList(categoryIds);
        if(code > 0){
            return  BaseApiResult.success("批量删除分类成功");
        }
        return  BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "删除分类失败");
    }

    @ApiOperation(value = "查询所有分类")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")
    @GetMapping(value = "/all")
    public BaseApiResult list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                              @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        List<Category> categories = categoryService.selectCategoryList(pageNum-1, pageSize);
        return BaseApiResult.success(categories);
    }

    @ApiOperation(value = "查询分类数量")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")
    @GetMapping(value = "/count")
    public BaseApiResult count() {
       int count = categoryService.getCategoryCount();
        return BaseApiResult.success(count);
    }
}

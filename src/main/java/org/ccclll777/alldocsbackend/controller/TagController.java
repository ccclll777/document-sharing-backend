package org.ccclll777.alldocsbackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.dao.TagDao;
import org.ccclll777.alldocsbackend.entity.Tag;
import org.ccclll777.alldocsbackend.entity.dto.TagDTO;
import org.ccclll777.alldocsbackend.enums.ErrorCode;
import org.ccclll777.alldocsbackend.service.TagService;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.ccclll777.alldocsbackend.utils.RegexConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/tag")
@Api(tags = "文档标签模块")
public class TagController {
    @Autowired
    private TagService tagService;
    @ApiOperation(value = "新增单个标签")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")
    @PostMapping(value = "/insert")
    public BaseApiResult insert(@RequestBody TagDTO tagDTO) {
        // 插入进来的参数必需经过清洗
        String name = tagDTO.getName();
        System.out.println(tagDTO.toString());
        if (!name.matches(RegexConstant.CH_ENG_WORD)) {
            return BaseApiResult.error(ErrorCode.PARAMS_CONTENT_ERROR.getCode(), ErrorCode.PARAMS_CONTENT_ERROR.getMessage());
        }
        Tag tag = Tag.builder().name(tagDTO.getName()).description(tagDTO.getDescription()).userId(tagDTO.getUserId()).build();
        int code = tagService.insertTag(tag);
        if(code > 0){
            return  BaseApiResult.success("插入标签成功");
        }
        return  BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "插入标签失败");
    }

    @ApiOperation(value = "更新标签")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")
    @PostMapping(value = "/update")
    public BaseApiResult update(@RequestBody TagDTO tagDTO) {
        // 插入进来的参数必需经过清洗
        String name = tagDTO.getName();
        if (!name.matches(RegexConstant.CH_ENG_WORD)) {
            return BaseApiResult.error(ErrorCode.PARAMS_CONTENT_ERROR.getCode(), ErrorCode.PARAMS_CONTENT_ERROR.getMessage());
        }
        Tag tag = Tag.builder().id(tagDTO.getId()).name(tagDTO.getName()).description(tagDTO.getDescription()).build();
        int code = tagService.updateTag(tag);
        if(code > 0){
            return  BaseApiResult.success("更新标签成功");
        }
        return  BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "更新标签失败");
    }

    @ApiOperation(value = "根据id移除某个分类")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
    @DeleteMapping(value = "/delete/{tagId}")
    public BaseApiResult remove(@PathVariable Integer tagId) {
        int code = tagService.deleteTag(tagId);
        if(code > 0){
            return  BaseApiResult.success("删除标签成功");
        }
        return  BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "删除标签失败");
    }

    @ApiOperation(value = "查询所有分类")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")
    @GetMapping(value = "/all")
    public BaseApiResult list(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                              @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        List<Tag> tags = tagService.selectTagList(pageNum-1, pageSize);
        return BaseApiResult.success(tags);
    }
    @ApiOperation(value = "查询标签数量")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")
    @GetMapping(value = "/count")
    public BaseApiResult count() {
        int count = tagService.getTagCount();
        return BaseApiResult.success(count);
    }

    @ApiOperation(value = "批量删除分类")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
    @DeleteMapping(value = "/deleteList/{tagIds}")
    public BaseApiResult removeList(@PathVariable String tagIds) {
        int code = tagService.deleteTagList(tagIds);
        if(code > 0){
            return  BaseApiResult.success("批量删除标签成功");
        }
        return  BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "删除标签失败");
    }

}

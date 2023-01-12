package org.ccclll777.alldocsbackend.controller;

import cn.hutool.crypto.SecureUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.entity.File;
import org.ccclll777.alldocsbackend.entity.FileDocument;
import org.ccclll777.alldocsbackend.entity.vo.FilesVO;
import org.ccclll777.alldocsbackend.entity.vo.SearchFilesVO;
import org.ccclll777.alldocsbackend.enums.ErrorCode;
import org.ccclll777.alldocsbackend.intercepter.SensitiveFilter;
import org.ccclll777.alldocsbackend.security.common.constants.SecurityConstants;
import org.ccclll777.alldocsbackend.security.common.utils.JwtTokenUtils;
import org.ccclll777.alldocsbackend.service.FileListService;
import org.ccclll777.alldocsbackend.service.FileService;
import org.ccclll777.alldocsbackend.service.TaskExecuteService;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/statistics")
@Api(tags = "统计模块")
public class StatisticsController {
    @Autowired
    private FileService fileService;
    @Autowired
    private FileListService fileListService;

    @ApiOperation(value = "查询最近上传的文档")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @GetMapping(value = "/recently")
    public BaseApiResult recently(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                              @RequestParam(value = "pageSize", defaultValue = "10") int pageSize)  {
        List<Map<String, Object>> files = fileListService.selectFilesRecently(pageSize);
        return BaseApiResult.success(files);
    }

    @ApiOperation(value = "热门文档")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @GetMapping(value = "/hot")
    public BaseApiResult hot(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize)  {
        Map<String, Object> top1 = fileListService.getTop1File();
        List<Object> others = fileListService.getFileByHits(pageNum,pageSize);
        Map<String, Object> result = Maps.newHashMap();
        result.put("top1", top1);
        result.put("others", others);
        return BaseApiResult.success(result);
    }

    @ApiOperation(value = "根据分类获取文档")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @GetMapping(value = "/filesByCategoryId")
    public BaseApiResult getFilesByCategoryId(
                             @RequestParam(value = "categoryId", defaultValue = "17") int categoryId,
                             @RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                             @RequestParam(value = "pageSize", defaultValue = "10") int pageSize)  {

        List<Object> files = fileListService.getFilesByCategoryId(categoryId,pageNum,pageSize);
        return BaseApiResult.success(files);
    }
}

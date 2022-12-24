package org.ccclll777.alldocsbackend.controller;

import cn.hutool.crypto.SecureUtil;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.entity.FileDocument;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.enums.ErrorCode;
import org.ccclll777.alldocsbackend.service.FileService;
import org.ccclll777.alldocsbackend.service.TaskExecuteService;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/files")
@Api(tags = "文档")
public class FileController {
    @Autowired
    private FileService fileService;
    @Autowired
    private TaskExecuteService taskExecuteService;

    /**
     * 表单上传文件
     * 当数据库中存在该md5值时，可以实现秒传功能
     *
     * @param file 文件
     * @return
     */
    @ApiOperation("上传文件")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    public BaseApiResult formUpload(@RequestParam("file") MultipartFile file) throws IOException {
        List<String> availableSuffixList = Lists.newArrayList("pdf", "png", "docx", "pptx", "xlsx");
            if (file != null && !file.isEmpty()) {
                String originFileName = file.getOriginalFilename();
                if (!StringUtils.hasText(originFileName)) {
                    return BaseApiResult.error(ErrorCode.UPLOAD_FAILED.getCode(),"格式不支持");
                }
                //获取文件后缀名
                String suffix = originFileName.substring(originFileName.lastIndexOf(".") + 1);
                if (!availableSuffixList.contains(suffix)) {
                    return BaseApiResult.error(ErrorCode.UPLOAD_FAILED.getCode(),"格式不支持");
                }
                String fileMd5 = SecureUtil.md5(file.getInputStream());
                FileDocument fileDocument = fileService.saveFile(fileMd5, file);
                switch (suffix) {
                    case "pdf":
                    case "docx":
                    case "pptx":
                    case "xlsx":
                        taskExecuteService.execute(fileDocument);
                        break;
                    default:
                        break;
                }
                return BaseApiResult.success("上传成功");
            } else {
                return BaseApiResult.error(ErrorCode.UPLOAD_FAILED.getCode(), "请传入文件");
            }

    }
//    @ApiOperation(value = "查询文档的分页列表页")
//    @PostMapping(value = "/list")
//    public BaseApiResult list(@RequestBody FileDTO documentDTO) throws IOException {
//        if (StringUtils.hasText(documentDTO.getFilterWord()) &&
//                documentDTO.getType() == FilterTypeEnum.FILTER) {
//            String filterWord = documentDTO.getFilterWord();
//            //非法敏感词汇判断
//            SensitiveFilter filter = SensitiveFilter.getInstance();
//            int n = filter.checkSensitiveWord(filterWord, 0, 1);
//            //存在非法字符
//            if (n > 0) {
//                log.info("这个人输入了非法字符--> {},不知道他到底要查什么~", filterWord);
//            }
//            redisService.incrementScoreByUserId(filterWord, RedisServiceImpl.SEARCH_KEY);
//        }
//        return iFileService.list(documentDTO);
//    }


}

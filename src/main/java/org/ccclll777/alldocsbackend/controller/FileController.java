package org.ccclll777.alldocsbackend.controller;

import cn.hutool.crypto.SecureUtil;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.entity.*;
import org.ccclll777.alldocsbackend.entity.dto.UpdateFileDTO;
import org.ccclll777.alldocsbackend.entity.dto.UploadDTO;
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
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/files")
@Api(tags = "文档")
public class FileController {
    @Autowired
    private FileService fileService;
    @Autowired
    private FileListService fileListService;
    @Autowired
    private TaskExecuteService taskExecuteService;

    /**
     * 表单上传文件
     * 当数据库中存在该md5值时，可以实现秒传功能
     *
     */
    @ApiOperation("上传文件")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    public BaseApiResult formUpload(@RequestParam("file") MultipartFile file, @RequestHeader(SecurityConstants.TOKEN_HEADER) String token) throws IOException {
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
                //需要根据token找到userId
                String tokenValue = token.replace(SecurityConstants.TOKEN_PREFIX, "");
                String userId =   JwtTokenUtils.getId(tokenValue);
                FileDocument fileDocument = fileService.saveFile(fileMd5, file,Integer.parseInt(userId),null);
                if(fileDocument != null) {
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
                    return BaseApiResult.error(ErrorCode.UPLOAD_FAILED.getCode(), "发生错误，上传失败");
                }

            } else {
                return BaseApiResult.error(ErrorCode.UPLOAD_FAILED.getCode(), "请传入文件");
            }

    }
    @ApiOperation(value = "查询文档的分页列表页")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
    @GetMapping(value = "/all")
    public BaseApiResult list(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                              @RequestParam(value = "pageSize", defaultValue = "10") int pageSize)  {
        List<FilesVO> files = fileService.selectFiles(pageNum-1, pageSize);
        return BaseApiResult.success(files);
    }
    @ApiOperation(value = "查询文档的分页列表页")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @GetMapping(value = "/userFileList")
    public BaseApiResult userFileList(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                              @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                      @RequestHeader(SecurityConstants.TOKEN_HEADER) String token      )  {
        //需要根据token找到userId
        String tokenValue = token.replace(SecurityConstants.TOKEN_PREFIX, "");
        String userId =   JwtTokenUtils.getId(tokenValue);
        List<FilesVO> files = fileService.selectFilesByUserId(pageNum-1, pageSize,Integer.parseInt(userId));
        return BaseApiResult.success(files);
    }

    @ApiOperation(value = "查询文档数量")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
    @GetMapping(value = "/count")
    public BaseApiResult count() {
        int count = fileService.fileCount();
        return BaseApiResult.success(count);
    }

    @ApiOperation(value = "查询用户的文档数量")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")
    @GetMapping(value = "/userFileCount")
    public BaseApiResult userFileCount(@RequestHeader(SecurityConstants.TOKEN_HEADER) String token) {
        //需要根据token找到userId
        String tokenValue = token.replace(SecurityConstants.TOKEN_PREFIX, "");
        String userId =   JwtTokenUtils.getId(tokenValue);
        int count = fileService.fileCountByUserId(Integer.parseInt(userId));
        return BaseApiResult.success(count);
    }

    @ApiOperation(value = "根据id彻底删除文档")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
    @DeleteMapping(value = "/deleteCompletely/{fileId}")
    public BaseApiResult deleteFileCompletely(@PathVariable Integer fileId) {
        int code = fileService.deleteFileCompletely(fileId);
        if(code > 0){
            return  BaseApiResult.success("彻底删除文档成功");
        }
        return  BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "删除文档失败");
    }

    @ApiOperation(value = "用户将文档状态改为已删除")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @DeleteMapping(value = "/delete/{fileId}")
    public BaseApiResult deleteFile(@PathVariable Integer fileId) {
        int code = fileService.deleteFile(fileId);
        System.out.println("deleteFile");
        if(code > 0){
            return  BaseApiResult.success("删除文档成功");
        }
        return  BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "删除文档失败");
    }

    @ApiOperation(value = "根据关键词查询文档信息")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @GetMapping(value = "/search")
    public BaseApiResult deleteFile(@RequestParam(value = "keyWord") String keyWord) throws IOException  {
        if (StringUtils.hasText(keyWord) ) {
            //非法敏感词汇判断
            SensitiveFilter filter = SensitiveFilter.getInstance();
            int n = filter.checkSensitiveWord(keyWord, 0, 1);
            //存在非法字符
            if (n > 0) {
                log.info("这个人输入了非法字符--> {},不知道他到底要查什么~", keyWord);
            }
        }
        List<SearchFilesVO> searchFilesVOS = fileService.search(keyWord);
        return  BaseApiResult.success(searchFilesVOS);
    }

    @ApiOperation(value = "搜索提示")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @GetMapping(value = "/hint")
    public BaseApiResult searchHint(@RequestParam(value = "keyWord") String keyWord) throws IOException  {
        if (StringUtils.hasText(keyWord) ) {
            //非法敏感词汇判断
            SensitiveFilter filter = SensitiveFilter.getInstance();
            int n = filter.checkSensitiveWord(keyWord, 0, 1);
            //存在非法字符
            if (n > 0) {
                log.info("这个人输入了非法字符--> {},不知道他到底要查什么~", keyWord);
            }
        }
        List<String> searchSuggests = fileService.searchSuggest(keyWord);
        return  BaseApiResult.success(searchSuggests);
    }

    @ApiOperation(value = "文档详细信息")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @GetMapping(value = "/detail")
    public BaseApiResult detail(@RequestParam(value = "mongoFileId") String mongoFileId)   {
        FilesVO filesVO = fileService.selectFIleByMongoFileId(mongoFileId);
        if (filesVO == null) {
            return  BaseApiResult.error(ErrorCode.FILE_NOT_FOUND.getCode(),ErrorCode.FILE_NOT_FOUND.getMessage());
        }
        return  BaseApiResult.success(filesVO);
    }

    @ApiOperation(value = "预览图")
    @GetMapping(value = "/image/{thumbId}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] previewThumb(@PathVariable String thumbId) {
        return fileService.getFileBytes(thumbId);
    }

    /**
     * 在线显示文件
     *
     * @param id 文件id
     * @return
     */
    @ApiOperation(value = "在线显示文件")
    @GetMapping("/view/{id}")
    public ResponseEntity<Object> serveFileOnline(@PathVariable String id) throws UnsupportedEncodingException {
        Optional<FileDocument> file = fileService.getById(id);
        if (file.isPresent()) {
            return ResponseEntity.ok()
                    // 这里需要进行中文编码
                    .header(HttpHeaders.CONTENT_DISPOSITION, "fileName=" + URLEncoder.encode(file.get().getName(), "utf-8"))
                    .header(HttpHeaders.CONTENT_TYPE, file.get().getContentType())
                    .header(HttpHeaders.CONTENT_LENGTH, file.get().getSize() + "").header("Connection", "close")
                    .header(HttpHeaders.CONTENT_LENGTH, file.get().getSize() + "")
                    .body(file.get().getContent());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorCode.FILE_NOT_FOUND);
        }
    }


    @ApiOperation("上传文件,携带表单")
    @PostMapping("/uploadWithForm")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    public BaseApiResult uploadWithForm(
            UploadDTO uploadDTO,
            @RequestParam(value = "file") MultipartFile file,
            @RequestHeader(SecurityConstants.TOKEN_HEADER) String token) throws IOException {
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
            //需要根据token找到userId
            String tokenValue = token.replace(SecurityConstants.TOKEN_PREFIX, "");
            String userId =   JwtTokenUtils.getId(tokenValue);
            FileDocument fileDocument = fileService.saveFile(fileMd5, file,Integer.parseInt(userId),uploadDTO);
            if(fileDocument != null) {
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
                return BaseApiResult.error(ErrorCode.UPLOAD_FAILED.getCode(), "发生错误，上传失败");
            }
        } else {
            return BaseApiResult.error(ErrorCode.UPLOAD_FAILED.getCode(), "请传入文件");
        }
    }
    @ApiOperation(value = "更新文档所属的分类和名称")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")
    @PostMapping(value = "/update")
    public BaseApiResult update(@RequestBody UpdateFileDTO updateFileDTO) {

        int code = fileService.updateFile(updateFileDTO);
        if(code > 0){
            return  BaseApiResult.success("更新文档成功");
        }
        return  BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), "更新文档失败");
    }
}

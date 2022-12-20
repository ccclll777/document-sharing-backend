package org.ccclll777.alldocsbackend.exception;

import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.enums.ErrorCode;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

/**
 * 全局统一异常处理
 * 捕获异常，产生异常时，统一返回错误信息
 *
 * @author jiarui.luo
 */
@Slf4j
@ResponseBody
@RestControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public BaseApiResult handleBaseException(BaseException e, HttpServletRequest request) {
        String message = e.getMessage() + request.getRequestURI();
        log.error("occur BaseException:{}" , message);
        return BaseApiResult.error(e.getErrorCode().getCode(), message);
    }


    @ExceptionHandler(value = Exception.class)
    public BaseApiResult handle(Exception e) {
        e.printStackTrace();
        if (e instanceof MaxUploadSizeExceededException) {
            log.error("occur MaxUploadSizeExceededException:{}" ,e.getMessage());
            return BaseApiResult.error(ErrorCode.UPLOAD_FAILED.getCode(), ErrorCode.UPLOAD_FAILED.getMessage());
        }else {
            log.error("occur Base Exception:{}" ,e.getMessage());
            return BaseApiResult.error(ErrorCode.OPERATE_FAILED.getCode(), ErrorCode.OPERATE_FAILED.getMessage());
        }
    }
    /**
     * 拦截valid参数校验返回的异常，并转化成基本的返回样式
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public BaseApiResult dealMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error("occur MethodArgumentNotValidException:{}" , defaultMessage);
        return BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), ErrorCode.PARAMS_PROCESS_FAILD.getMessage());
    }

    /**
     * validation 效验post or get 方式表单方式提交转对象，效验出错
     **/
    @ExceptionHandler(BindException.class)
    public BaseApiResult handleValidation(BindException e) {
        String defaultMessage = e.getFieldError().getDefaultMessage();
        log.error("occur handleValidation:{}" , defaultMessage);
        return BaseApiResult.error(ErrorCode.OPERATE_FAILED.getCode(), ErrorCode.OPERATE_FAILED.getMessage());
    }

    /**
     * 拦截valid参数校验返回的异常，并转化成基本的返回样式
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public BaseApiResult dealConstraintViolationException(ConstraintViolationException e) {
        log.error("occur dealConstraintViolationException:{}" , e.getMessage());
        return BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), ErrorCode.PARAMS_PROCESS_FAILD.getMessage());
    }

    /**
     * 参数类型转换错误
     */
    @ExceptionHandler(HttpMessageConversionException.class)
    public BaseApiResult parameterTypeException(HttpMessageConversionException e) {
        log.error("occur HttpMessageConversionException:{}" , e.getMessage());
        return BaseApiResult.error(ErrorCode.PARAMS_TYPE_ERROR.getCode(), e.getMessage());
    }

    /**
     * 请求方法不正确
     **/
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public BaseApiResult dealHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("occur HttpRequestMethodNotSupportedException:{}" , e.getMessage());
        return BaseApiResult.error(ErrorCode.OPERATE_FAILED.getCode(), ErrorCode.OPERATE_FAILED.getMessage());
    }

    @ExceptionHandler(value = UserNameAlreadyExistException.class)
    public BaseApiResult handleUserNameAlreadyExistException(UserNameAlreadyExistException e) {
        log.error("occur UserNameAlreadyExistException:" + e.getMessage());
        return BaseApiResult.error(e.getErrorCode().getCode(), e.getMessage());
    }

    @ExceptionHandler(value = {RoleNotFoundException.class, UserNameNotFoundException.class})
    public BaseApiResult handleUserNotFoundException(BaseException e, HttpServletRequest request) {
        String message = e.getMessage() + request.getRequestURI();
        log.error("occur ResourceNotFoundException:{}" , message);
        return BaseApiResult.error(e.getErrorCode().getCode(), e.getMessage());
    }



}



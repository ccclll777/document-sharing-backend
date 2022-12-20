package org.ccclll777.alldocsbackend.exception;

import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.enums.ErrorCode;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.jdbc.*;
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
//        System.out.println(e.getClass());
        if (e instanceof MaxUploadSizeExceededException) {
            log.error("occur MaxUploadSizeExceededException:{}" ,e.getMessage());
            return BaseApiResult.error(ErrorCode.UPLOAD_FAILED.getCode(), ErrorCode.UPLOAD_FAILED.getMessage());
        } else {
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

    /**
     * 数据库语法错误异常
     */
    @ExceptionHandler(value = BadSqlGrammarException.class)
    public BaseApiResult handleBadSqlGrammarException(BadSqlGrammarException e) {
        log.error("occur BadSqlGrammarException:{}" , e.getMessage());
        return BaseApiResult.error(ErrorCode.SQL_ERROR.getCode(), ErrorCode.SQL_ERROR.getMessage());
    }

    /**
     * 数据库连接错误异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = CannotGetJdbcConnectionException.class)
    public BaseApiResult handleCannotGetJdbcConnectionException(CannotGetJdbcConnectionException e) {
        log.error("occur CannotGetJdbcConnectionException:{}" , e.getMessage());
        return BaseApiResult.error(ErrorCode.SQL_ERROR.getCode(), ErrorCode.SQL_ERROR.getMessage());
    }

    /**
     * 数据库结果计数异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = IncorrectResultSetColumnCountException.class)
    public BaseApiResult handleIncorrectResultSetColumnCountException(IncorrectResultSetColumnCountException e) {
        log.error("occur IncorrectResultSetColumnCountException:{}" , e.getMessage());
        return BaseApiResult.error(ErrorCode.SQL_ERROR.getCode(), ErrorCode.SQL_ERROR.getMessage());
    }

    /**
     * 无效结果集访问异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = InvalidResultSetAccessException.class)
    public BaseApiResult handleInvalidResultSetAccessException(InvalidResultSetAccessException e) {
        log.error("occur InvalidResultSetAccessException:{}" , e.getMessage());
        return BaseApiResult.error(ErrorCode.SQL_ERROR.getCode(), ErrorCode.SQL_ERROR.getMessage());
    }

    /**
     * Lob 检索失败异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = LobRetrievalFailureException.class)
    public BaseApiResult handleLobRetrievalFailureException(LobRetrievalFailureException e) {
        log.error("occur LobRetrievalFailureException:{}" , e.getMessage());
        return BaseApiResult.error(ErrorCode.SQL_ERROR.getCode(), ErrorCode.SQL_ERROR.getMessage());
    }

    /**
     * SQL 警告异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = SQLWarningException.class)
    public BaseApiResult handleSQLWarningException(SQLWarningException e) {
        log.error("occur SQLWarningException:{}" , e.getMessage());
        return BaseApiResult.error(ErrorCode.SQL_ERROR.getCode(), ErrorCode.SQL_ERROR.getMessage());
    }

    /**
     * 未分类的SQL异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = UncategorizedSQLException.class)
    public BaseApiResult handleUncategorizedSQLException(UncategorizedSQLException e) {
        log.error("occur UncategorizedSQLException:{}" , e.getMessage());
        return BaseApiResult.error(ErrorCode.SQL_ERROR.getCode(), ErrorCode.SQL_ERROR.getMessage());
    }



}



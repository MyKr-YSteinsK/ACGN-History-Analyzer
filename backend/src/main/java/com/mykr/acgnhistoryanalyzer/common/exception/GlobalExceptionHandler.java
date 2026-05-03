package com.mykr.acgnhistoryanalyzer.common.exception;

import com.mykr.acgnhistoryanalyzer.common.response.ApiResponse;
import com.mykr.acgnhistoryanalyzer.config.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("请求参数校验失败");

        return ApiResponse.fail(4000, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMostSpecificCause().getMessage());
        return ApiResponse.fail(4001, "请求体格式不正确或无法解析为 JSON");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ApiResponse.fail(4005, "不支持的请求方法");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleNoResourceFound(NoResourceFoundException ex) {
        return ApiResponse.fail(4040, "资源不存在");
    }

    @ExceptionHandler(BangumiImportException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ApiResponse<?> handleBangumiImportException(BangumiImportException ex) {
        return ApiResponse.fail(5020, ex.getMessage());
    }

    @ExceptionHandler(LlmAnalysisException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ApiResponse<?> handleLlmAnalysisException(LlmAnalysisException ex) {
        return ApiResponse.fail(5030, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleAny(Exception ex, HttpServletRequest request) {
        String requestId = MDC.get(RequestIdFilter.MDC_REQUEST_ID);
        if (requestId == null) {
            requestId = "unknown";
        }

        log.error("Unhandled error {} {} ref={}", request.getMethod(), request.getRequestURI(), requestId, ex);

        String hint = "服务器内部错误，参考编号：" + requestId;
        return ApiResponse.fail(5000, hint);
    }
}

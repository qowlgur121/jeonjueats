package com.jeonjueats.exception;

import com.jeonjueats.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 애플리케이션 전역에서 발생하는 예외를 처리하는 글로벌 예외 핸들러
 * 
 * 모든 컨트롤러에서 발생하는 예외를 여기서 잡아서 일관된 형식으로 응답합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 비즈니스 예외 처리
     * 우리가 정의한 BusinessException이 발생했을 때 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business Exception: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(e.getErrorCode(), e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 입력값 검증 실패 예외 처리 
     * @Valid 어노테이션으로 검증 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation Exception: {}", e.getMessage());
        
        // 첫 번째 필드 에러 메시지 사용
        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);
        String message = fieldError.getDefaultMessage();
        
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE.getCode(), message);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 지원하지 않는 HTTP 메서드 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("Method Not Supported Exception: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED.getCode(), ErrorCode.METHOD_NOT_ALLOWED.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }
    
    /**
     * 파일 업로드 크기 초과 예외 처리
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("File Size Exceeded Exception: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.FILE_SIZE_EXCEEDED.getCode(), ErrorCode.FILE_SIZE_EXCEEDED.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 일반적인 예외 처리
     * 위에서 처리되지 않은 모든 예외를 여기서 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        log.error("Unexpected Exception: ", e);  // 스택 트레이스까지 로그에 남김
        
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
} 
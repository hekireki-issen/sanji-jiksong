package hekireki.sanjijiksong.global.common.exception.advice;

import hekireki.sanjijiksong.global.common.exception.*;
import hekireki.sanjijiksong.global.common.exception.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ApiExceptionAdvice {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse response = new ErrorResponse("INVALID_ARGUMENT", ex.getMessage());
        log.error("IllegalArgumentException: {}", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<Object> handleUserException(UserException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getErrorCode().getStatus());
    }

    //store 관련
    @ExceptionHandler(StoreException.class)
    public ResponseEntity<ErrorResponse> handleStoreException(StoreException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(new ErrorResponse(e.getErrorCode()));
    }

    //order
    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponse> handleUserException(OrderException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getErrorCode().getStatus());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException e){
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(new ErrorResponse(e.getErrorCode()));
    }
  
    @ExceptionHandler(KamisException.class)
    public ResponseEntity<ErrorResponse> handleKamisException(KamisException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(new ErrorResponse(e.getErrorCode()));
    }
}

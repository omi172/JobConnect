package com.jobconnect.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public String handleUnauthorized(UnauthorizedActionException ex, Model model) {
        log.warn("Unauthorized action: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }
}

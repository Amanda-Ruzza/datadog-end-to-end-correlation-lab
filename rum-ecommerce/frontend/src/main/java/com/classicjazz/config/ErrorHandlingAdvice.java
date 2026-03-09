package com.classicjazz.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * Global exception handling: show a friendly error page instead of the whitelabel one.
 * Similar to handling exceptions in Python (e.g. FastAPI exception handlers).
 */
@ControllerAdvice
public class ErrorHandlingAdvice {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandlingAdvice.class);

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleAnyException(Throwable ex, HttpServletRequest request) {
        log.warn("Unhandled error for {}: {}", request.getRequestURI(), ex.getMessage());
        if (log.isDebugEnabled()) {
            log.debug("Full stack trace", ex);
        }
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.");
        return mav;
    }

    @ExceptionHandler(TemplateProcessingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleTemplateError(TemplateProcessingException ex, HttpServletRequest request) {
        log.warn("Template error for {}: {}", request.getRequestURI(), ex.getMessage());
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", "A page template error occurred. We've been notified.");
        return mav;
    }
}

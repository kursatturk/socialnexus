package com.socialnexus.ui;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.socialnexus.auth.UnauthorizedSessionException;
import com.socialnexus.exception.BadRequestException;
import com.socialnexus.exception.ConflictException;
import com.socialnexus.exception.ResourceNotFoundException;

/**
 * Returns HTML alert fragments for HTMX UI requests instead of JSON API errors.
 */
@ControllerAdvice(assignableTypes = UiHtmxController.class)
public class UiExceptionHandler {

    @ExceptionHandler(UnauthorizedSessionException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorized(UnauthorizedSessionException ex, Model model) {
        model.addAttribute("errorMessage", "Session expired. Please sign in again.");
        return "fragments/alert :: error";
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "fragments/alert :: error";
    }

    @ExceptionHandler({ConflictException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "fragments/alert :: error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "fragments/alert :: error";
    }
}

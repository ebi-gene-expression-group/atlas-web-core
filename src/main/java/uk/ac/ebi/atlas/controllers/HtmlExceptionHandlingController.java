package uk.ac.ebi.atlas.controllers;

import com.google.common.base.Joiner;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
class HtmlExceptionHandlingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlExceptionHandlingController.class);

    @ExceptionHandler({
        ResourceNotFoundException.class,
        BioentityNotFoundException.class,
        NoHandlerFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFoundExceptions(Exception e) {
        return handleException(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {UnparseableSemanticQueryException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ModelAndView handleBadJsonException(Exception e) {
        return handleException(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ModelAndView handleExceptionFallback(Exception e) {
        LOGGER.error("{} - {}", e.getMessage(), Joiner.on("\n\t").join(e.getStackTrace()));
        return handleException(e, HttpStatus.BAD_REQUEST);
    }

    private @NotNull ModelAndView handleException(Exception e, HttpStatus status) {
        ModelAndView mav = new ModelAndView("error-page");
        mav.addObject("exceptionMessage", e.getMessage());
        mav.addObject("statusCode", status.value());
        mav.addObject("title", "Error: " + status.value()); // Added more informative title

        return mav;
    }
}

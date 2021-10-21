package com.ford.vdcc.poc;/*
package com.ford.vdcc.internalvaluedashapi;


import com.ford.vdcc.internalvaluedashapi.dto.GenericResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
class GlobalDefaultExceptionHandler {
    public static final String DEFAULT_ERROR_VIEW = "error";

    @ExceptionHandler(value = Exception.class)
    public GenericResponse
    defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        System.out.println("Exception :: ");
        //if (AnnotationUtils.findAnnotation
        //        (e.getClass(), ResponseStatus.class) != null)
        //    throw e;

        // Otherwise setup and send the user to a default error-view.

        return  new GenericResponse("0000",e.getMessage());
    }
}
*/

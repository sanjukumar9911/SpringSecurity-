package com.ford.vdcc.poc.batch.util;

import com.ford.cloudnative.base.api.BaseBodyError;
import com.ford.cloudnative.base.api.BaseBodyResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletResponse;


@ControllerAdvice
public class ResponseBodyAdviceCustom<T> implements ResponseBodyAdvice<T> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public T beforeBodyWrite(T body, MethodParameter returnType, MediaType selectedContentType,
                             Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
       System.out.println("ResponseBodyAdviceCustom Called :: "+servletResponse.getStatus());

       int errorStatus = servletResponse.getStatus();
       if(errorStatus!=0 && errorStatus > 300) {
           // Depending on the status code we will send an Error message to Notify Event Mail API.
           String message = body.toString();
           BaseBodyResponse resp = (BaseBodyResponse) body;
           BaseBodyError bodyErr = resp.getError();

           System.out.println("Base Body Response STARTED:::: ");
           System.out.println("Error Code :: "+bodyErr.getErrorCode());
           System.out.println("Error Message :: "+bodyErr.getMessages());
           System.out.println("Base Body Response END :::: ");

           //BaseBodyResponse(result=null, error=BaseBodyError(errorCode=null, messages=[Error in Method :: ], dataErrors=[], attributes={exception=java.lang.Exception, referenceId=a7746a43552f255b, timestamp=1623072354}))
           System.out.println("BODY :: "+message);

       }


        return body;
    }

}
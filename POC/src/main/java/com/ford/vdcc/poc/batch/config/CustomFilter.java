package com.ford.vdcc.poc.batch.config;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


@Component
public class CustomFilter  implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        System.out.println("FILER Is" +
                " being called ... ::: " );

        HttpSession session = req.getSession();


        String requestURI = req.getRequestURI();


        if(response!=null) {
            HttpServletResponse res = (HttpServletResponse) response;

            System.out.println("FILER Is" +
                    " being called ... ::: " + res.getStatus());

            if (res.getStatus() != 0) {

            }
        }

        chain.doFilter(request, response);
    }
    @Override
    public void destroy() {
        System.out.println("Destroy filter: {}"+ getClass().getSimpleName());
    }

}

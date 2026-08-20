package com.rsifatimah.keperawatan.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("*.xhtml")
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Init if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String loginURI = req.getContextPath() + "/login.xhtml";

        boolean loggedIn = (session != null && session.getAttribute("loggedUser") != null);
        boolean loginRequest = req.getRequestURI().equals(loginURI);
        boolean resourceRequest = req.getRequestURI().contains("javax.faces.resource") ||
                                  req.getRequestURI().contains("/resources/");

        if (loggedIn || loginRequest || resourceRequest) {
            chain.doFilter(request, response);
        } else {
            res.sendRedirect(loginURI);
        }
    }

    @Override
    public void destroy() {
        // Destroy if needed
    }
}

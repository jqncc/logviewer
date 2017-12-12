package org.jflame.logviewer.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jflame.logviewer.util.Config;
import org.jflame.web.filter.IgnoreUrlMatchFilter;

@WebFilter(filterName = "loginFiler", urlPatterns = "/*", initParams = {
        @WebInitParam(name = "ignorePattern", value = "/login(\\.jsp){0,1}") })
public class LoginFilter extends IgnoreUrlMatchFilter {

    private String loginPage = "/login.jsp";// 登录页url

    public void destroy() {
    }

    @Override
    protected void doInternalFilter(ServletRequest req, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession();
        Map<String,String> user = (Map<String,String>) session.getAttribute(Config.SESSION_CURRENT_USER);
        if (user != null) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendRedirect(request.getContextPath() + loginPage);
        }
    }

    @Override
    protected void doInternalInit(FilterConfig filterConfig) {
    }

}

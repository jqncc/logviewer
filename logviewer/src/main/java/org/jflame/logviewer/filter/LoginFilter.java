package org.jflame.logviewer.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.CallResult.ResultEnum;
import org.jflame.logviewer.SysParam;
import org.jflame.web.WebUtils;
import org.jflame.web.filter.IgnoreUrlMatchFilter;

// @WebFilter(urlPatterns = { "*.do" })
public class LoginFilter extends IgnoreUrlMatchFilter {

    private String loginPage = "/index.html";// 登录页url

    @Override
    protected void doInternalFilter(ServletRequest req, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession();
        if (session.getAttribute(SysParam.SESSION_CURRENT_USER) != null) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse res = (HttpServletResponse) response;
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            if (WebUtils.isAjaxRequest(request)) {
                WebUtils.outJson(res, new CallResult<>(ResultEnum.NO_AUTH));
            } else {
                res.sendRedirect(request.getContextPath() + loginPage);
            }
        }
    }

    @Override
    protected void doInternalInit(FilterConfig filterConfig) {
    }

}

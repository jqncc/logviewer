package org.jflame.logviewer.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jflame.commons.crypto.DigestHelper;
import org.jflame.commons.model.CallResult;
import org.jflame.commons.util.StringHelper;
import org.jflame.logviewer.SysParam;
import org.jflame.web.WebUtils;

// @WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public LoginServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uname = request.getParameter("uname");
        String upwd = request.getParameter("upwd");
        if (StringHelper.isNotEmpty(uname) && StringHelper.isNotEmpty(upwd)) {
            if (checkUser(uname.trim(), upwd.trim())) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                Map<String,String> user = new HashMap<>();
                user.put("name", uname);
                session = request.getSession(true);
                session.setAttribute(SysParam.SESSION_CURRENT_USER, user);
                WebUtils.outJson(response, new CallResult<>());
                return;
            }
        }
        WebUtils.outJson(response, new CallResult<>().paramError("用户名或密码不正确"));
        return;
    }

    private boolean checkUser(String loginUser, String loginPwd) {
        String user = SysParam.getUser();
        String pwd = SysParam.getUserpwd();
        String cryptPwd = DigestHelper.sha256Hex(loginPwd + "&&" + loginUser);
        if (user.equals(loginUser) && cryptPwd.equalsIgnoreCase(pwd)) {
            return true;
        }
        return false;
    }

    /* public static void main(String[] args) {
        // System.out.println(DigestHelper.sha256Hex("look@2020&&loger"));
    
        Path classRunDir = Paths.get("D:\\repository\\ant\\ant\\1.6.5\\ant-1.6.5.jar");
        if (classRunDir.toString().endsWith(".jar")) {
            classRunDir = classRunDir.getParent();
            if (classRunDir.endsWith("1.6.5")) {
                System.out.println(classRunDir.getParent());// 打包以jar方式运行,在lib目录下
            }
        }
        System.out.println("--" + classRunDir);
    }*/
}

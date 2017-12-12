package org.jflame.logviewer.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jflame.logviewer.util.Config;
import org.jflame.toolkit.crypto.DigestHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public LoginServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uname = request.getParameter("uname");
        String upwd = request.getParameter("upwd");
        if (StringHelper.isEmpty(uname) || StringHelper.isEmpty(upwd)) {
            request.setAttribute("errmsg", "用户名和密码不能为空");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } else {
            String pwdCrypt = DigestHelper.shaHex(upwd.trim());
            if (Config.user.equals(uname.trim()) && Config.pwd.equals(pwdCrypt)) {
                HttpSession session = request.getSession();
                session.invalidate();
                session = request.getSession(true);
                Map<String,String> user = new HashMap<>();
                user.put("name", uname);
                session.setAttribute(Config.SESSION_CURRENT_USER, user);
                response.sendRedirect(request.getContextPath() + "/index.jsp");
            }
        }

    }

    public static void main(String[] args) {
        System.out.println(DigestHelper.shaHex("viewer17"));
        System.out.println(Config.encrypt("ghg889900"));
    }
}

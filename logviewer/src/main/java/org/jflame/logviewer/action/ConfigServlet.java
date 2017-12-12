package org.jflame.logviewer.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jflame.logviewer.util.Config;
import org.jflame.toolkit.util.JsonHelper;
import org.jflame.web.util.WebUtils;

/**
 * Servlet implementation class ConfigServlet
 */
@WebServlet("/config")
public class ConfigServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public ConfigServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cmd = request.getParameter("cmd");
        if ("proj".equals(cmd)) {
            WebUtils.outJson(response, JsonHelper.toJson(Config.PROJ_INFOS));
        } else if (("server".equals(cmd))) {
            WebUtils.outJson(response, JsonHelper.toJson(Config.SERVER_INFOS));
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

}

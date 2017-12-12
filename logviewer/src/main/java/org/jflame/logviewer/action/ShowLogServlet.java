package org.jflame.logviewer.action;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jflame.logviewer.util.Config;
import org.jflame.toolkit.codec.TranscodeHelper;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.web.util.webfile.DownloadUtils;

@SuppressWarnings("serial")
@WebServlet("/logView")
public class ShowLogServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cmd = request.getParameter("cmd");
        String logPath = request.getParameter("f");
        Path targetPath = Paths.get(Config.LOG_BASE_FOLDER, TranscodeHelper.urldecode(logPath));

        if ("down".equals(cmd)) {
            DownloadUtils.download(response, targetPath.toString());
        } else if ("view".equals(cmd)) {
            String content = FileHelper.readText(targetPath.toString(), StandardCharsets.UTF_8.name());
            response.setContentType("text/plain");
            response.getWriter().println(content);
            response.getWriter().close();
        }
    }

}

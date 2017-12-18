package org.jflame.logviewer.action;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jflame.logviewer.model.FileAttri;
import org.jflame.logviewer.model.ProjLogInfo;
import org.jflame.logviewer.util.Config;
import org.jflame.toolkit.codec.TranscodeHelper;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.toolkit.valid.ValidatorHelper;
import org.jflame.web.config.ConfigKey;
import org.jflame.web.config.ServletParamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class ShowLogFileServlet
 */
@SuppressWarnings("serial")
public class ShowLogFileServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ShowLogFileServlet.class);

    private ConfigKey<String> LOG_PATH_KEY = new ConfigKey<>("logPath");
    private File baseFile;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletParamConfig servletParam = new ServletParamConfig(config);
        Config.LOG_BASE_FOLDER = servletParam.getString(LOG_PATH_KEY);
        if (StringHelper.isEmpty(Config.LOG_BASE_FOLDER)) {
            logger.error("未指定日志文件路径");
        } else {
            baseFile = Paths.get(Config.LOG_BASE_FOLDER).toFile();
            if (!baseFile.exists()) {
                logger.warn("日志文件路径{}不存在", Config.LOG_BASE_FOLDER);
            }
        }
    }

    /**
     * 查询项目下的日志文件
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String projId = request.getParameter("projId");
        final ProjLogInfo projLogInfo = Config.getProjById(projId);
        Map<String,List<FileAttri>> logFileMap = new HashMap<>(projLogInfo.getLogPaths().length);
        if (projLogInfo != null) {
            request.setAttribute("currentProj", projLogInfo);
            // 按路径,提取日志文件
            for (String projPath : projLogInfo.getLogPaths()) {
                List<FileAttri> attris = new ArrayList<>();
                Path targetPath = Paths.get(projPath);
                if (!targetPath.isAbsolute()) {
                    targetPath = Paths.get(Config.LOG_BASE_FOLDER, projPath);
                }
                File logDir = targetPath.toFile();
                if (logDir.exists()) {
                    File[] targetFiles;
                    if (StringHelper.isEmpty(projLogInfo.getNameFilter())) {
                        targetFiles = logDir.listFiles();
                    } else {
                        targetFiles = logDir.listFiles(new FilenameFilter() {

                            @Override
                            public boolean accept(File dir, String name) {
                                if (ValidatorHelper.regex(name, projLogInfo.getNameFilter())) {
                                    return true;
                                }
                                return false;
                            }
                        });
                    }
                    if (targetFiles != null) {
                        FileAttri attri;
                        for (File file : targetFiles) {
                            attri = new FileAttri();
                            attri.setName(file.getName());
                            attri.setLastUpdateDate(file.lastModified());
                            attri.setSize(file.length() / 1024 + "K");
                            attri.setPath(TranscodeHelper.urlencode(Paths.get(projPath, file.getName()).toString()));
                            attris.add(attri);
                        }
                    }

                    if (!attris.isEmpty()) {
                        Collections.sort(attris);
                        logFileMap.put(projPath, attris);
                    }
                } else {
                    logger.error("{}配置的日志文件夹不存在{}", projLogInfo.getProjName(), targetPath.toString());
                }
            }

            request.setAttribute("logFileMap", logFileMap);
        }
        request.getRequestDispatcher("/logFileList.jsp").forward(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        doGet(request, response);
    }

}

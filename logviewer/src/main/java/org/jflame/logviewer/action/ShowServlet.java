package org.jflame.logviewer.action;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jflame.commons.codec.TranscodeHelper;
import org.jflame.commons.exception.RemoteAccessException;
import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.CallResult.ResultEnum;
import org.jflame.commons.model.TreeNode;
import org.jflame.commons.util.ArrayHelper;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.IOHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.file.FileHelper;
import org.jflame.commons.util.file.ZipHelper;
import org.jflame.logviewer.ServerCfg;
import org.jflame.logviewer.SysParam;
import org.jflame.logviewer.model.FileAttri;
import org.jflame.logviewer.model.Server;
import org.jflame.logviewer.ssh.SFTPClient;
import org.jflame.logviewer.ssh.SSHClientFactory;
import org.jflame.web.WebUtils;

// @WebServlet("/show.do")
public class ShowServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    ExecutorService executorService;

    public ShowServlet() {
        executorService = Executors.newFixedThreadPool(2, new BasicThreadFactory.Builder().daemon(true).build());
    }

    @Override
    public void destroy() {
        executorService.shutdown();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cmd = request.getParameter("obj");
        if (("server".equals(cmd))) {
            CallResult<List<Server>> result = CallResult.ok(ServerCfg.getServers());
            WebUtils.outJson(response, result);
        } else if ("down".equals(cmd)) {
            // CallResult<Object> result = new CallResult<>();
            downloadLog(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cmd = request.getParameter("obj");
        CallResult<Object> result = new CallResult<>(ResultEnum.FAILED);
        if ("file".equals(cmd)) {
            lsFiles(request, response, result);
        }
        WebUtils.outJson(response, result);
    }

    /**
     * 列出日志文件目录树
     * 
     * @param request
     * @param response
     * @param result
     * @throws IOException
     */
    private void lsFiles(HttpServletRequest request, HttpServletResponse response, CallResult<Object> result)
            throws IOException {
        List<FileAttri> fileAttris = new ArrayList<>();
        String ip = request.getParameter("ip");
        Optional<Server> selectedServer = ServerCfg.getServer(ip);
        if (!selectedServer.isPresent()) {
            result.paramError(ip + "服务器未配置");
            return;
        }

        String user = request.getParameter("su");
        String password = request.getParameter("sp");
        Server connServer = selectedServer.get();
        boolean isNeedSaveCfg = false;
        if (StringHelper.isEmpty(selectedServer.get().getUser())
                || StringHelper.isEmpty(selectedServer.get().getPwd())) {
            if (StringHelper.isEmpty(user) || StringHelper.isEmpty(password)) {
                result.status(4001).message("请输入远程连接用户和密码");
                return;
            } else {
                connServer.setUser(user);
                connServer.setPwd(password);
                isNeedSaveCfg = true;
            }
        }

        String[] dirs = connServer.dirs();
        String[] excludes = connServer.excludeLogs();
        try {
            if (ArrayHelper.isNotEmpty(dirs)) {
                SFTPClient client = SSHClientFactory.getFtpClient(request.getSession(false).getId(), connServer);
                FileAttri root = null;
                for (String dir : dirs) {
                    root = new FileAttri();
                    root.setLabel(dir);
                    root.setPath(dir);
                    root.setState(TreeNode.STATE_OPEN);
                    root.addAttribute("dir", true);
                    root.setId(Math.abs(dir.hashCode()));

                    List<FileAttri> lst = client.ls(dir, excludes);

                    if (CollectionHelper.isNotEmpty(lst)) {
                        root.addNodes(lst);
                    }
                    fileAttris.add(root);
                }
                result.setResult(ResultEnum.SUCCESS);
                result.setData(fileAttris);
                if (isNeedSaveCfg) {
                    ServerCfg.save();
                }
            } else {
                result.paramError("未配置日志目录");
            }
        } catch (RemoteAccessException e) {
            if (e.getStatusCode() == 4001) {
                connServer.setUser(null);
                connServer.setPwd(null);
                result.status(4001).message(e.getMessage());
            } else {
                result.error(e.getMessage());
            }
        }
    }

    private void downloadLog(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String downFile = request.getParameter("f");
        if (StringHelper.isEmpty(downFile)) {
            response.getWriter().print("请选择要下载的文件");
            return;
        }
        Optional<Server> selectedServer = ServerCfg.getServer(request.getParameter("ip"));
        if (!selectedServer.isPresent()) {
            response.getWriter().print("服务器未配置");
            return;
        }

        downFile = TranscodeHelper.urlDecode(downFile);
        if (!"log".equals(FileHelper.getExtension(downFile, false))) {
            response.getWriter().print("不允许下载的文件类型");
            return;
        }
        Server connServer = selectedServer.get();

        boolean baseDirOk = connServer.isCanRead(downFile);
        if (!baseDirOk) {
            response.getWriter().print("文件路径不正确");
            return;
        }
        ServletOutputStream output = null;
        InputStream input = null;
        String zipFile = null;
        try {
            String downFileDir = FileHelper.getDir(downFile);
            Path downFileDirPath = Paths.get(SysParam.TMP_DIR, connServer.getIp(), downFileDir);
            Files.createDirectories(downFileDirPath);
            String downFilename = FileHelper.getFilename(downFile);

            SFTPClient client = SSHClientFactory.getFtpClient(request.getSession(false).getId(), connServer);
            if ("1".equals(request.getParameter("enablezip"))) {
                // 压缩后再传
                String dstFile = downFileDirPath.resolve(downFilename).toString();
                String dstZipFilename = downFilename + ".zip";

                client.download(downFile, dstFile);
                zipFile = ZipHelper.zip(dstFile, downFileDirPath.toString(), dstZipFilename, false,
                        StandardCharsets.UTF_8);

                input = Files.newInputStream(Paths.get(zipFile));
                WebUtils.setFileDownloadHeader(response, dstZipFilename, null);
                output = response.getOutputStream();
                IOHelper.copy(input, output, new byte[8096]);
                output.flush();
            } else {
                byte[] downBytes = client.getFile(downFile);
                WebUtils.setFileDownloadHeader(response, downFilename, (long) downBytes.length);
                output = response.getOutputStream();
                IOHelper.write(downBytes, output);
                output.flush();
            }

        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            IOHelper.closeQuietly(input);
            if (zipFile != null) {
                executorService.execute(new DeleteFileThread(zipFile));
            }
        }
    }

    private class DeleteFileThread implements Runnable {

        private String deleteFile;

        public DeleteFileThread(String _deleteFile) {
            deleteFile = _deleteFile;
        }

        @Override
        public void run() {
            FileHelper.deleteQuietly(deleteFile);
        }
    }
}

package org.jflame.logviewer.action;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jflame.commons.codec.TranscodeHelper;
import org.jflame.commons.exception.RemoteAccessException;
import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.CallResult.ResultEnum;
import org.jflame.commons.util.ArrayHelper;
import org.jflame.commons.util.CharsetHelper;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.IOHelper;
import org.jflame.commons.util.MapHelper;
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
        String parentId = request.getParameter("id");
        Optional<Server> selectedServer = ServerCfg.getServer(ip);
        if (!selectedServer.isPresent()) {
            result.paramError(ip + "服务器未配置");
            return;
        }

        // String user = request.getParameter("su");
        // String password = request.getParameter("sp");

        Server connServer = selectedServer.get();
        boolean isNeedSaveCfg = false;
        if (StringHelper.isEmpty(selectedServer.get().getUser())
                || StringHelper.isEmpty(selectedServer.get().getPwd())) {
            String tmpSu = request.getParameter("su");
            if (StringHelper.isEmpty(tmpSu)) {
                result.status(4001).message("请输入远程连接用户和密码");
                return;
            }
            String namePwdBase64 = CharsetHelper.getUtf8String(TranscodeHelper.dencodeBase64(tmpSu));
            String[] tmpNps = namePwdBase64.split("__");
            String user = tmpNps[0];
            String password = tmpNps[1];
            if (StringHelper.isEmpty(tmpSu) || StringHelper.isEmpty(password)) {
                result.status(4001).message("请输入远程连接用户和密码");
                return;
            } else {
                connServer.setUser(user);
                connServer.setPwd(password);
                isNeedSaveCfg = true;
            }
        }
        HttpSession session = request.getSession();
        String sessKey = ip + "dirmap";
        String[] dirs = null;
        Integer pid;
        Map<Integer,String> idDirMap = null;
        Map<String,Integer> dirIdMap = new HashMap<>();
        if (StringHelper.isNotEmpty(parentId)) {
            // 根据id获得路径
            pid = Integer.valueOf(parentId);
            idDirMap = (Map<Integer,String>) session.getAttribute(sessKey);
            if (!MapHelper.isEmpty(idDirMap)) {
                String parentDir = idDirMap.get(pid);
                if (parentDir != null) {
                    dirs = new String[]{ parentDir };
                    dirIdMap.put(parentDir, pid);
                    // idDirMap.put(pid, parentDir);
                } else {
                    log("列出目录失败,在session中未有父级id缓存");
                    result.paramError("目录不存在");
                    return;
                }
            } else {
                log("列出目录失败,在session中未有目录缓存");
                result.paramError("目录不存在");
                return;
            }
        } else {
            // 根目录开始
            dirs = connServer.dirs();
            if (ArrayHelper.isNotEmpty(dirs)) {
                idDirMap = new HashMap<>();
                for (int i = 0; i < dirs.length; i++) {
                    idDirMap.put(i + 1, dirs[i]);
                    dirIdMap.put(dirs[i], i + 1);
                }
                Map<Integer,String> oldDirMap = (Map<Integer,String>) session.getAttribute(sessKey);
                if (MapHelper.isNotEmpty(oldDirMap)) {
                    oldDirMap.putAll(idDirMap);
                    session.setAttribute(sessKey, oldDirMap);
                } else {
                    session.setAttribute(sessKey, idDirMap);
                }
            }
        }
        String[] excludes = connServer.excludeLogs();
        try {
            if (ArrayHelper.isNotEmpty(dirs)) {
                SFTPClient client = SSHClientFactory.getFtpClient(request.getSession(false).getId(), connServer);
                int j = 1;
                for (String dir : dirs) {
                    List<FileAttri> lst = client.ls(dir, false, excludes);
                    if (CollectionHelper.isNotEmpty(lst)) {
                        fileAttris.addAll(lst);
                        int startId = dirIdMap.get(dir) * 10;
                        j = 0;
                        for (FileAttri fa : lst) {
                            if (fa.isDir()) {
                                j++;
                                fa.setId(startId + j);// 重新设置个id,供前端使用
                                idDirMap.put(fa.getId(), fa.getPath());
                            }
                        }
                    }
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

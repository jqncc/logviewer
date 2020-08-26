package org.jflame.logviewer.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jflame.commons.codec.TranscodeHelper;
import org.jflame.commons.exception.BusinessException;
import org.jflame.commons.exception.RemoteAccessException;
import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.CallResult.ResultEnum;
import org.jflame.commons.model.TreeNode;
import org.jflame.commons.util.ArrayHelper;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.IOHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.file.FileHelper;
import org.jflame.logviewer.ServerCfg;
import org.jflame.logviewer.model.FileAttri;
import org.jflame.logviewer.model.Server;
import org.jflame.logviewer.ssh.SFTPClient;
import org.jflame.logviewer.ssh.SSHClientFactory;
import org.jflame.web.WebUtils;

// @WebServlet("/show.do")
public class ShowServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cmd = request.getParameter("obj");
        if (("server".equals(cmd))) {
            CallResult<List<Server>> result = CallResult.ok(ServerCfg.getServers());
            WebUtils.outJson(response, result);
        } else if ("down".equals(cmd)) {
            CallResult<Object> result = new CallResult<>();
            downloadLog(request, response, result);
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
        String ip = request.getParameter("ip").trim();
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

        try {
            String[] dirs = connServer.dirs();
            if (ArrayHelper.isNotEmpty(dirs)) {
                SFTPClient client = SSHClientFactory.getFtpClient(request.getSession(false).getId(), connServer);
                if (isNeedSaveCfg) {
                    ServerCfg.save();
                }
                FileAttri root = null;
                for (String dir : dirs) {
                    root = new FileAttri();
                    root.setLabel(dir);
                    root.setPath(dir);
                    root.setState(TreeNode.STATE_OPEN);
                    root.addAttribute("dir", true);
                    root.setId(Math.abs(dir.hashCode()));
                    List<FileAttri> lst = client.ls(dir);
                    if (CollectionHelper.isNotEmpty(lst)) {
                        root.addNodes(lst);
                    }
                    fileAttris.add(root);
                }
                result.setResult(ResultEnum.SUCCESS);
                result.setData(fileAttris);
            } else {
                result.paramError("未配置日志目录");
            }
        } catch (BusinessException e) {
            if (e.getStatus() == 4001) {
                connServer.setUser(null);
                connServer.setPwd(null);
            }
            result.result(e);
        } catch (RemoteAccessException e) {
            result.error(e.getMessage());
        }
    }

    private void downloadLog(HttpServletRequest request, HttpServletResponse response, CallResult<Object> result)
            throws ServletException, IOException {
        String downFile = request.getParameter("f");
        if (StringHelper.isEmpty(downFile)) {
            result.paramError("请选择要下载的文件");
            return;
        }
        String ip = request.getParameter("ip").trim();
        Optional<Server> selectedServer = ServerCfg.getServer(ip);
        if (!selectedServer.isPresent()) {
            WebUtils.outJson(response, result.paramError(ip + "服务器未配置"));
            return;
        }

        downFile = TranscodeHelper.urlDecode(downFile);
        if (!"log".equals(FileHelper.getExtension(downFile, false))) {
            result.paramError("不允许下载的文件类型");
            return;
        }
        Server connServer = selectedServer.get();
        String[] dirs = connServer.dirs();
        boolean baseDirOk = false;
        for (String d : dirs) {
            if (downFile.startsWith(d)) {
                baseDirOk = true;
                break;
            }
        }
        if (!baseDirOk) {
            result.paramError("文件路径不正确");
            return;
        }

        ServletOutputStream output = null;
        try {
            SFTPClient client = SSHClientFactory.getFtpClient(request.getSession(false).getId(), connServer);
            byte[] downBytes = client.getFile(downFile);
            WebUtils.setFileDownloadHeader(response, FileHelper.getFilename(downFile), (long) downBytes.length);
            output = response.getOutputStream();
            IOHelper.write(downBytes, output);
            output.flush();
        } catch (RemoteAccessException e) {
            result.status(e.getStatusCode() > 0 ? e.getStatusCode() : ResultEnum.SERVER_ERROR.getStatus())
                    .message(e.getMessage());
        } catch (Exception e) {
            result.status(ResultEnum.SERVER_ERROR.getStatus()).message(e.getMessage());
        }

    }
}
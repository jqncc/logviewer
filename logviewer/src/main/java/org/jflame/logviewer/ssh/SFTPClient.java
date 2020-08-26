package org.jflame.logviewer.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jflame.commons.exception.PermissionException;
import org.jflame.commons.exception.RemoteAccessException;
import org.jflame.commons.model.TreeNode;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.IOHelper;
import org.jflame.commons.util.MathHelper;
import org.jflame.commons.util.UrlHelper;
import org.jflame.logviewer.model.FileAttri;
import org.jflame.logviewer.model.Server;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class SFTPClient extends BaseJchClient {

    public SFTPClient(Server serverInfo) throws RemoteAccessException {
        super(serverInfo);
    }

    private ChannelSftp openChannel() throws RemoteAccessException {
        conn();
        try {
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(DEFAULT_CONN_TIMEOUT);
            return channel;
        } catch (JSchException e) {
            throw new RemoteAccessException(e);
        }
    }

    /**
     * 从sftp服务器下载指定文件到本地指定目录
     * 
     * @param remoteFile 文件的绝对路径+fileName
     * @param localPath 本地临时文件路径
     * @return
     */
    public boolean download(String remoteFile, String localPath) throws RemoteAccessException {
        ChannelSftp sftp = null;
        try {
            sftp = openChannel();
            sftp.get(remoteFile, localPath);
            return true;
        } catch (SftpException e) {
            logger.error("download remoteFile:{},localPath:{}, ex:{}", remoteFile, localPath, e);
            throw new RemoteAccessException(e);
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }

    /**
     * 读取sftp上指定文件数据
     * 
     * @param remoteFile
     * @return
     */
    public byte[] getFile(String remoteFile) throws RemoteAccessException {
        ChannelSftp sftp = null;
        InputStream inputStream = null;
        try {
            sftp = openChannel();
            inputStream = sftp.get(remoteFile);
            return IOHelper.readBytes(inputStream);
        } catch (SftpException | IOException e) {
            logger.error("getFile remoteFile:{},ex:{}", remoteFile, e);
            throw new RemoteAccessException(e);
        } finally {
            IOHelper.closeQuietly(inputStream);
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }

    /**
     * 读取sftp上指定（文本）文件数据,并按行返回数据集合
     *
     * @param remoteFile
     * @param charsetName
     * @return
     */
    public String getFileContent(String remoteFile, Charset charset) throws RemoteAccessException {
        ChannelSftp sftp = null;
        InputStream inputStream = null;
        try {
            sftp = openChannel();
            inputStream = sftp.get(remoteFile);
            return IOHelper.readText(inputStream, charset);
        } catch (SftpException | IOException e) {
            logger.error("getFileText remoteFile:{},error:{}", remoteFile, e);
            throw new RemoteAccessException(e);
        } finally {
            IOHelper.closeQuietly(inputStream);
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }

    /**
     * 列出指定目录下文件列表
     * 
     * @param remotePath
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<FileAttri> ls(String remotePath) throws RemoteAccessException {
        ChannelSftp sftp = null;
        List<FileAttri> lsFiles = new ArrayList<>();
        FileAttri tmpFileAttri;
        long tmpSize;
        try {
            sftp = openChannel();
            Vector<LsEntry> vector = sftp.ls(remotePath);
            for (LsEntry entry : vector) {
                if (".".equals(entry.getFilename()) || "..".equals(entry.getFilename())) {
                    continue;
                }

                tmpFileAttri = new FileAttri();
                tmpFileAttri.setLabel(entry.getFilename());
                tmpFileAttri.setLastUpdateDate(entry.getAttrs().getATime());
                tmpFileAttri.setPath(UrlHelper.mergeUrl(remotePath, entry.getFilename()));
                tmpFileAttri.setId(Math.abs(tmpFileAttri.getPath().hashCode()));
                tmpFileAttri.addAttribute("dir", false);
                // System.out.println(entry.getFilename() + " p:" + entry.getAttrs().getPermissions());

                if (entry.getAttrs().isDir()) {
                    tmpFileAttri.setState(TreeNode.STATE_CLOSED);
                    tmpFileAttri.addAttribute("dir", true);
                    try {
                        List<FileAttri> childs = ls(tmpFileAttri.getPath());
                        if (CollectionHelper.isNotEmpty(childs)) {
                            tmpFileAttri.addNodes(childs);
                        }
                    } catch (PermissionException e) {
                        tmpFileAttri.setLabel(tmpFileAttri.getLabel() + "[无权限]");
                    }
                } else {
                    tmpFileAttri.setState(TreeNode.STATE_OPEN);
                    tmpSize = entry.getAttrs().getSize();
                    if (tmpSize < 1024) {
                        tmpFileAttri.setSize(entry.getAttrs().getSize() + "B");
                    } else if (tmpSize >= 1024 && tmpSize < 1048576) {
                        tmpFileAttri.setSize(MathHelper.round((entry.getAttrs().getSize() / 1024f), 1) + "KB");
                    } else if (tmpSize > 1048576) {
                        tmpFileAttri.setSize(MathHelper.round((entry.getAttrs().getSize() / 1048576f), 2) + "MB");
                    }
                }
                lsFiles.add(tmpFileAttri);
            }
        } catch (SftpException e) {
            logger.error("ls remotePath:{} , error:{}", remotePath, e.getMessage());
            if ("Permission denied".equals(e.getMessage())) {
                throw new PermissionException("没有文件读取权限");
            }
            throw new RemoteAccessException(e);
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
        }
        return lsFiles;
    }

}

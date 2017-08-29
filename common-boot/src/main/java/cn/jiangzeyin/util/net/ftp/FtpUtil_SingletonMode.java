package cn.jiangzeyin.util.net.ftp;//package com.yoke.net.ftp;
//
//import com.yoke.UtilSystemCache;
//import com.yoke.util.StringUtil;
//import com.yoke.util.file.FileUtil;
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPFile;
//import org.apache.commons.net.ftp.FTPReply;
//import org.springframework.util.Assert;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.SocketException;
//import java.net.SocketTimeoutException;
//import java.util.LinkedList;
//
///**
// * ftp 工具单例模式
// *
// * create by XiangZhongBao on2017-3-6
// */
//public class FtpUtil_SingletonMode {
//
//    private static final String fileEncodingName = "ISO-8859-1";
//
//    /**
//     * 创建ftp 连接对象
//     *
//     * @param server
//     * @param port
//     * @param username
//     * @param password
//     * @param mode
//     * @return
//     * @throws SocketException
//     * @throws IOException
//     * @author jiangzeyin
//     * @date 2016-9-8
//     */
//    public FTPClient getFtpClient(String server, int port, String username, String password, String encoding, int mode) throws IOException {
//        YokeFtp ftp = new YokeFtp();
//        // ftp.configure(config)
//        ftp.setDefaultTimeout(40000);
//        ftp.setDefaultPort(port);
//        ftp.connect(server);
//        ftp.setControlEncoding(encoding);
//        ftp.login(username, password);
//        ftp.setSoTimeout(40000);
//        ftp.setConnectTimeout(40000);
//        ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
//        if (mode == 1)
//            ftp.enterLocalPassiveMode();// 被动模式
//        else
//            ftp.enterLocalActiveMode();// 主动模式
//        String FtpRoot = ftp.printWorkingDirectory();
//        ftp.changeWorkingDirectory(FtpRoot);
//        return ftp;
//    }
//
//    /**
//     * 获取ftp 连接对象
//     *
//     * @param ftpInfo
//     * @return
//     * @throws IOException
//     * @author jiangzeyin
//     * @date 2016-10-21
//     */
//    public YokeFtp getFtpClient(FtpInfo ftpInfo) throws IOException {
//        YokeFtp ftp = new YokeFtp();
//        ftp.setDefaultPort(ftpInfo.getPort());
//        ftp.connect(ftpInfo.getIp());
//        ftp.setControlEncoding(ftpInfo.getEncoding());
//        // FTP服务器连接回答
//        if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
//            return null;
//        }
//        // 登录
//        ftp.login(ftpInfo.getUserName(), ftpInfo.getUserPwd());
//        ftp.setSoTimeout(ftpInfo.getTimeOut());
//        ftp.setConnectTimeout(ftpInfo.getTimeOut());
//        if (ftpInfo.getMode() == 1)
//            ftp.enterLocalPassiveMode();// 被动模式
//        else
//            ftp.enterLocalActiveMode();// 主动模式
//        ftp.setSchemeId(ftpInfo.getId());
//        ftp.setDataTimeout(ftpInfo.getTimeOut());
//        return ftp;
//    }
//
//    /**
//     * 发送文件到Ftp
//     *
//     * @param ftpClient
//     * @param locpath   本地文件目录
//     * @param spli      远程文件截取目录
//     * @return boolean
//     * @author XiangZhongBao
//     * @date 2017-3-1
//     */
//    public Boolean fileSendFtp(YokeFtp ftpClient, String locpath, String spli) throws IOException {
//        if (ftpClient == null || StringUtil.isEmpty(locpath) || StringUtil.isEmpty(spli)) {
//            return false;
//        }
//        int defaultErrorCount = 0;/*默认允许出错从试的次数*/
//        FileUtil fileUtil = new FileUtil();
//        LinkedList<File> linkedList = FileUtil.getFolderFiles(locpath);
//        if (linkedList != null) {
//            for (int i = 0, flen = linkedList.size(); i < flen; i++) {
//                File file = linkedList.get(i);
//                String lopath = StringUtil.cleanPath(file.getAbsolutePath());
//                String repath = lopath.substring(lopath.indexOf(spli));
//                String rpath = repath.substring(0, repath.lastIndexOf("/") + 1);
//                String rname = repath.substring(repath.lastIndexOf("/") + 1);
//                if (file.exists()) {
//                    try {
//                        if (defaultErrorCount >= 3) {
//                            UtilSystemCache.getInstance().LOG_INFO().info("sendFile:" + lopath+"错误次数过高,退出发布！");
//                            return false;
//                        }
//                        UtilSystemCache.getInstance().LOG_INFO().info("send:" + i + " " + lopath);
//                        if (!FtpUpload(ftpClient, file, rpath, rname)) {
//                            i--;
//                            defaultErrorCount++;
//                            continue;
//                        }
//                    } catch (SocketTimeoutException e) {
//                        UtilSystemCache.getInstance().LOG_ERROR().error("警告：send:" + i + "---" + lopath + "---读取超时", e);
//                        i-=2;
//                        defaultErrorCount++;
//                        continue;
//                    } catch (SocketException se) {
//                        UtilSystemCache.getInstance().LOG_ERROR().error("错误！链接被重置" + ftpClient.toString(), se);
//                        return false;
//                    }
//                } else {
//                    UtilSystemCache.getInstance().LOG_INFO().info("错误！文件不存在！send:" + i + " " + lopath);
//                    return false;
//                }
//            }
//            UtilSystemCache.getInstance().LOG_INFO().info("send:" + linkedList.size() + "个文件---全部完成");
//            return true;
//        }
//        UtilSystemCache.getInstance().LOG_INFO().info("错误！" + locpath + "目录下没有文件存在！");
//        return false;
//    }
//
//    /**
//     * ftp 上传文件 文件名为本地文件名
//     *
//     * @param ftp
//     * @param fileName
//     * @param remotePath
//     * @return
//     * @throws IOException
//     * @author jiangzeyin
//     * @date 2016-11-12
//     */
//    public boolean FtpUpload(YokeFtp ftp, String fileName, String remotePath) throws IOException {
//        return FtpUpload(ftp, new File(fileName), remotePath);
//    }
//
//    /**
//     * ftp 发布单个文件
//     *
//     * @param ftp
//     * @param fileName
//     * @param rootPath
//     * @param remotePath
//     * @return
//     * @throws IOException
//     * @author jiangzeyin
//     * @date 2016-11-14
//     */
//    public boolean FtpUpload(YokeFtp ftp, String fileName, String rootPath, String remotePath) throws IOException {
//        File file = new File(fileName);
//        String path = FileUtil.getFilePath(file).replace(rootPath, "");
//        remotePath = String.format("%s/%s", StringUtil.convertNULL(remotePath), path);
//        return FtpUpload(ftp, file, remotePath);
//    }
//
//    /**
//     * ftp 上传文件 文件名为本地文件名
//     *
//     * @param ftp
//     * @param file
//     * @param remotePath
//     * @return
//     * @throws IOException
//     * @author jiangzeyin
//     * @date 2016-11-12
//     */
//    public boolean FtpUpload(YokeFtp ftp, File file, String remotePath) throws IOException {
//        return FtpUpload(ftp, file, remotePath, null);
//    }
//
//    /**
//     * ftp 上传单个文件 文件名为指定 如没有指定则默认为本地文件名
//     *
//     * @param ftp
//     * @param file       文件对象
//     * @param remotePath ftp 文件路径
//     * @param remoteName ftp 文件名  默认为本地文件名
//     * @return
//     * @throws IOException
//     * @author jiangzeyin
//     * @date 2016-11-12
//     */
//    public boolean FtpUpload(YokeFtp ftp, File file, String remotePath, String remoteName) throws IOException {
//        Assert.notNull(ftp, "ftp 信息不能为kong");
//        Assert.notNull(file, "上传文件不能为空");
//        // 文件合法性检查
//        if (!file.exists() || file.isDirectory()) {
//            throw new IllegalArgumentException("ftp 上传当个文件不存在或者为一个文件夹");
//        }
//        // 切换路径
//        boolean flag = changeWorkingDirectory(ftp, remotePath);
//        if (!flag) {
//            UtilSystemCache.getInstance().LOG_INFO().info(String.format("ftp切换目录失败,文件:%s", remotePath));
//            return false;
//        }
//        ftp.setBufferSize(4096);// 4096比1024*1024速度要快
//        ftp.setControlKeepAliveTimeout(500);
//        ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
//        InputStream is = new FileInputStream(file);
//        // 文件名
//        String OnlyFileName;
//        if (StringUtil.isEmpty(remoteName)) {
//            OnlyFileName = new String(file.getName().getBytes(), fileEncodingName);
//        } else {
//            OnlyFileName = new String(remoteName.getBytes(), fileEncodingName);
//        }
//        boolean storeFile = ftp.storeFile(OnlyFileName, is);
//        if (!storeFile) {
//            UtilSystemCache.getInstance().LOG_INFO().info(String.format("ftp上传失败,文件:%s", storeFile));
//        }
//        is.close();
//        return storeFile;
//    }
//
//    /**
//     * ftp 上传文件夹
//     *
//     * @param ftp
//     * @param rootPath
//     * @return
//     * @throws IOException
//     * @author jiangzeyin
//     * @date 2016-11-11
//     */
//    public boolean FtpUpload(YokeFtp ftp, String rootPath) throws IOException {
//        return FtpUpload(ftp, rootPath, false);
//    }
//
//    /**
//     * 上传文件 到指定文件夹下
//     *
//     * @param ftp
//     * @param rootPath
//     * @param remotePath
//     * @param containName 是否包含文件夹名
//     * @return
//     * @throws IOException
//     * @author jiangzeyin
//     * @date 2016-11-12
//     */
//    public boolean FtpUpload(YokeFtp ftp, String rootPath, String remotePath, boolean containName) throws IOException {
//        File file = new File(rootPath);
//        if (!file.exists())
//            return false;
//        if (file.isDirectory()) {
//            return UploadDirectoryPath(ftp, rootPath, file.getPath(), remotePath, containName);
//        } else {
//            return FtpUploadFile(ftp, file, rootPath, remotePath, containName);
//        }
//    }
//
//    /**
//     * @param ftp
//     * @param rootPath
//     * @param currPath
//     * @param remotePath
//     * @param containName
//     * @return
//     * @throws IOException
//     */
//    private boolean UploadDirectoryPath(YokeFtp ftp, String rootPath, String currPath, String remotePath, boolean containName) throws IOException {
//        File file = new File(currPath);
//        if (!file.exists())
//            return false;
//        if (!file.isDirectory())
//            return false;
//        File[] files = file.listFiles();
//        if (files == null)
//            return false;
//        for (File item : files) {
//            if (item.isDirectory()) {
//                // 继续上传目录
//                if (!UploadDirectoryPath(ftp, rootPath, item.getPath(), remotePath, containName))
//                    return false;
//            } else {
//                // 处理单个文件
//                boolean flag = FtpUploadFile(ftp, item, rootPath, remotePath, containName);
//                if (!flag)
//                    return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * ftp 上传文件夹 到根路径
//     *
//     * @param ftp
//     * @param rootPath
//     * @param containName
//     * @return
//     * @throws IOException
//     * @author jiangzeyin
//     * @date 2016-11-11
//     */
//    public boolean FtpUpload(YokeFtp ftp, String rootPath, boolean containName) throws IOException {
//        return FtpUpload(ftp, rootPath, "/", containName);
//    }
//
//    /**
//     * 处理上传文件信息
//     *
//     * @param ftp
//     * @param file
//     * @param rootPath    本地根路径
//     * @param remotePath  上传到远程路径
//     *                    默认为根路径
//     * @param containName 是否包含文件夹名称
//     * @return
//     * @throws IOException
//     * @author jiangzeyin
//     * @date 2016-11-12
//     */
//    private boolean FtpUploadFile(YokeFtp ftp, File file, String rootPath, String remotePath, boolean containName) throws IOException {
//        Assert.notNull(file, "上传文件不能为空");
//        if (StringUtil.isEmpty(remotePath)) {
//            remotePath = "/";
//        }
//        String newRemotePath = FileUtil.getFilePath(file).replace(rootPath, "");
//        newRemotePath = String.format("%s/%s", remotePath, newRemotePath);
//        if (containName) {
//            String name = new File(rootPath).getName();
//            newRemotePath = String.format("%s/%s", name, newRemotePath);
//        }
//        // 上传文件
//        return FtpUpload(ftp, file, newRemotePath);
//    }
//
//    /**
//     * 切换ftp 工作路径
//     *
//     * @param ftpClient
//     * @return
//     * @throws IOException
//     * @author jiangzeyin
//     * @date 2016-11-11
//     */
//    private boolean changeWorkingDirectory(FTPClient ftpClient, String remotePath) throws IOException {
//        String directory = FileUtil.clearPath(remotePath);
//        // 如果是根路径
//        if (directory.equals("/")) {
//            ftpClient.changeWorkingDirectory("/");
//            return true;
//        }
//        // 切换到根路径
//        if (directory.startsWith("/")) {
//            ftpClient.changeWorkingDirectory("/");
//        }
//        // 切换到对应路径
//        if (!ftpClient.changeWorkingDirectory(new String(directory.getBytes(), fileEncodingName))) {
//            // 如果远程目录不存在，则递归创建远程服务器目录
//            String[] paths = StringUtil.StringToArray(directory, "/");
//            for (String string : paths) {
//                String subDirectory = new String(string.getBytes(), fileEncodingName);
//                if (!ftpClient.changeWorkingDirectory(subDirectory)) {
//                    if (ftpClient.makeDirectory(subDirectory)) {
//                        if (!ftpClient.changeWorkingDirectory(subDirectory))
//                            return false;
//                    } else {
//                        UtilSystemCache.getInstance().LOG_ERROR().error("ftp 目录创建失败", new RuntimeException(subDirectory));
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    /**
//     * ftp删除文件及文件夹
//     *
//     * @param ftp
//     * @param pathName
//     * @return
//     * @throws IOException
//     * @date 2016-9-12
//     */
//    public boolean removeDirectoryALLFile(FTPClient ftp, String pathName) throws IOException {
//        FTPFile[] files = ftp.listFiles(pathName);
//        if (null != files && files.length > 0) {
//            for (FTPFile file : files) {
//                if (file.isDirectory()) {
//                    removeDirectoryALLFile(ftp, pathName + "/" + file.getName());
//                    // 切换到父目录，不然删不掉文件夹
//                    ftp.changeWorkingDirectory(pathName.substring(0, pathName.lastIndexOf("/")));
//                    ftp.removeDirectory(pathName);
//                } else {
//                    if (!ftp.deleteFile(pathName + "/" + file.getName())) {
//                        return false;
//                    }
//                }
//            }
//        }
//        // 切换到父目录，不然删不掉文件夹
//        ftp.changeWorkingDirectory(pathName.substring(0, pathName.lastIndexOf("/")));
//        ftp.removeDirectory(pathName);
//        return true;
//    }
//
//    /**
//     * 删除文件-FTP方式
//     *
//     * @param ftp  FTPClient对象
//     * @param path FTP服务器上传地址
//     * @return
//     * @throws IOException
//     */
//    public boolean deleteFile(FTPClient ftp, String path) throws IOException {
//        return ftp.deleteFile(path);
//    }
//
//    /**
//     * 关闭连接，使用完连接之后，一定要关闭连接，否则服务器会抛出 Connection reset by peer的错误
//     *
//     * @param ftpClient
//     * @author jiangzeyin
//     * @date 2016-10-21
//     */
//    public void closeConnection(YokeFtp ftpClient) {
//        if (ftpClient == null) {
//            return;
//        }
//        try {
//            ftpClient.logout();
//            ftpClient.disconnect();
//        } catch (IOException e) {
//            UtilSystemCache.getInstance().LOG_ERROR().error("关闭ftp 连接错误", e);
//        }
//    }
//
//    /**
//     * 关闭连接，使用完连接之后，一定要关闭连接，否则服务器会抛出 Connection reset by peer的错误
//     *
//     * @param ftpClients
//     * @author jiangzeyin
//     * @date 2016-10-21
//     */
//    public void closeConnection(YokeFtp[] ftpClients) {
//        if (ftpClients == null)
//            return;
//        for (YokeFtp ftpClient2 : ftpClients) {
//            closeConnection(ftpClient2);
//        }
//    }
//}
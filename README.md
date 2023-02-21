## 项目以转到gitee : https://gitee.com/jqncc/logviewer

# logviewer

一个简单的服务器日志查看系统,实现日志文件的在线浏览和下载,无需安装代理在线查看实时日志(类似tail命令效果). 不想搭建复杂的ELK可以试用下

# 技术实现

1. jsch库实现SSH远程连接,执行命令和sftp下载文件
2. websocket+SSH tail命令实现在浏览器实时查看日志(安全原因,客户端只允许执行tail,cat,head查看命令)
3. 使用嵌入式tomcat,打包即可执行,无需特殊配置

# 配置文件

1. system.properties
   * 配置登录的用户和密码,tomcat的端口,应用路径等. 密码加密方式:DigestHelper.sha256Hex("密码&&用户名")
  
2. cfg.xml
   * 配置各服务器的远程登录信息,三个属性:ip,port,log_dir. log_dir需要查看的日志目录,多个可以使用逗号分隔.
   * 首次登录远程服务器,会要求输入远程登录的用户和密码,系统会将密码加密保存到配置文件中,后续远程连接时会先取配置文件中的密码

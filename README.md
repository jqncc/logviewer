# logviewer

一个简单的服务器日志查看系统,实现日志文件的在线浏览和下载,应用实时日志在线查看。使用技术:java servlet + jsch + websocket

1. 通过rsync+sersync将各服务器日志文件同步集中到一台服务器,按ip划分文件夹
2. 通过websocket+SSH tail命令,在浏览器实时查看多台服务器(tomcat或其他可通过tail查看)日志

适用小型公司有多台服务器,日志量不多的情况


# 配置文件介绍

1. proj.json 项目信息配置,主要用于查看同步过来的日志文件,通过rsync+sersync将各服务器日志文件同步,目录结构:根目录->各服务器ip->服务器下项目名
  配置属性:
 *  logPaths[],日志文件相对与日志根目录的路径,可以有多个,比较为集群应用每个服务器下都有日志
 *  projName,项目名
 *  projId,项目唯一id
 *  nameFilter,取文件名的正则表达式,如同一文件夹但有几个项目的日志或其他文件可以用名称过滤,不需要过滤则为空字符
  
  
2. server.json 服务器信息配置,配置属性:

  *  desc,描述
  *  ip,服务器ip
  *  name,服务器名称
  *  user,远程登录用户名
  *  pwd,远程登录密码
  *  tomcats[],服务器内部署的tomcat或者任何有日志输出的独立程序
  
         *  dir, 程序根目录
         *  name,程序名
         *  port,程序端口
         *  desc,描述
         *  console,实时日志输出文件路径,如果是tomcat不填时默认是dir+logs/catalina.out


                  

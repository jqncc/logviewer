<%@page import="org.jflame.logviewer.util.Config,org.jflame.logviewer.model.Server"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="error.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%String ctx = request.getContextPath();
pageContext.setAttribute("ctx", ctx);
String basePath=request.getServerName();
// 80,443不显示端口号
if (request.getServerPort() != 80 && !(request.getServerPort() == 443 && "https".equals(request.getScheme()))) {
    basePath = basePath + ":" + request.getServerPort();
}
basePath= basePath + request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>tomcat实时日志</title>
<link rel="stylesheet" type="text/css" href="css/common.css">
<link rel="stylesheet" type="text/css" href="${ctx}/js/plugins/easyui/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" href="${ctx}/js/plugins/easyui/themes/icon.css">
<script type="text/javascript" src="${ctx}/js/jquery.min.js"></script>
<script type="text/javascript" src="${ctx}/js/plugins/easyui/jquery.easyui.min.js"></script>
<script type="text/javascript" src="${ctx}/js/plugins/easyui/locale/easyui-lang-zh_CN.js"></script>
<script type="text/javascript">var contextPath="${ctx}";</script>
</head>
<body class="easyui-layout">
<%
String ip=request.getParameter("ip");
Server server = Config.getServerByIp(ip);
if(server==null){
    out.print(ip+"服务器不存在");
    out.close();
}
request.setAttribute("ts",server.getTomcats());
%>
<div id="tt" class="easyui-tabs" data-options="border:true,fit:true,plain:true">
    <c:forEach items="${ts}" var="t">
    <div title="${t.name}_${t.port}" fit="true">
        <div style="background-color: #f5f5f5;color:#000;padding:10px">${t.desc}</div>
		<iframe style="width:98%;height:95%" frameborder="0" src="console.jsp?ip=<%=ip%>&port=${t.port}"></iframe>
    </div>
    </c:forEach>
</div>
</body>
</html>
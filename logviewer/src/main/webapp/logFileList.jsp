<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="error.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%String ctx = request.getContextPath();
pageContext.setAttribute("ctx", ctx);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>项目日志文件浏览</title>
<link rel="stylesheet" type="text/css" href="css/common.css">
<link rel="stylesheet" type="text/css" href="${ctx}/js/plugins/easyui/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" href="${ctx}/js/plugins/easyui/themes/icon.css">
<script type="text/javascript" src="${ctx}/js/jquery.min.js"></script>
<script type="text/javascript" src="${ctx}/js/plugins/easyui/jquery.easyui.min.js"></script>
<script type="text/javascript" src="${ctx}/js/plugins/easyui/locale/easyui-lang-zh_CN.js"></script>
<script type="text/javascript">var contextPath="${ctx}";</script>
</head>
<body>
<c:if test="${not empty logFileMap}" var="hasLog">
<div id="tt" class="easyui-tabs" data-options="border:true,fit:true,plain:true">
   <c:forEach items="${logFileMap}" var="entry">
    <div title="${entry.key}" style="padding:10px 20px">
    <table class="tablelist">
        <thead>
            <tr><th width="300">名称</th><th width="200">修改日期</th><th width="100">大小</th><th width="100">操作</th></tr>
        </thead>
        <c:forEach items="${entry.value}" var="logFile">
        <tr>
        <td>${logFile.name}</td><td>${logFile.lastUpdateDateText}</td><td>${logFile.size}</td>
        <td> 
        <a href="${ctx}/logView?cmd=view&f=${logFile.path}" target="_blank" title="如文件较大请下载后查看">查看</a>&nbsp;&nbsp; 
        <a href="${ctx}/logView?cmd=down&f=${logFile.path}" target="_blank">下载</a></td>
        </tr>
        </c:forEach>
    </table>
    </div>
    </c:forEach>
</div>
</c:if>
<c:if test="${!hasLog}">
<div style="text-align:center">暂无日志</div>
</c:if>
<table>
</table>
</body>
</html>
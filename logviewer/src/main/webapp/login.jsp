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
<title>实时日志查看-登录</title>
<link rel="stylesheet" type="text/css" href="css/common.css">
<link rel="stylesheet" type="text/css" href="${ctx}/js/plugins/easyui/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" href="${ctx}/js/plugins/easyui/themes/icon.css">
<script type="text/javascript" src="${ctx}/js/jquery.min.js"></script>
<script type="text/javascript" src="${ctx}/js/plugins/easyui/jquery.easyui.min.js"></script>
<script type="text/javascript" src="${ctx}/js/plugins/easyui/locale/easyui-lang-zh_CN.js"></script>
<style type="text/css">
body{text-align: center;}
#login p{padding:10px;text-align: left}
</style>
<script type="text/javascript">
var contextPath="${ctx}";
function submitForm(){
    if(!$('#frm').form("validate")){
        return;
    }
    $('#frm').submit();
}
</script>
</head>
<body>
<div style="width:400px;padding:10px;margin: 200px auto">
<div id="login" class="easyui-panel" title="实时日志查看系统-登录"
    data-options="iconCls:'icon-save',closable:false" style="width:100%;height:250px;background-color: #EFF5FF">
    <form id="frm" method="post" action="${ctx}/login" style="padding:25px 50px">
        <p style="color:red;padding:5px">${errmsg}</p>
	    <p><input class="easyui-textbox" name="uname" value="${uname}" label="用户名:" labelWidth="60" style="width:250px;height:30px" data-options="required:true" iconCls="icon-man" ></p>
	    <p><input class="easyui-passwordbox" name="upwd" value="${upwd}" label="密 码:" labelWidth="60" style="width:250px;height:30px" data-options="required:true" iconCls="icon-lock"></p>
	    <p style="margin-top:10px;padding-left:70px">
	    <a href="javascript:void(0)" class="easyui-linkbutton" onclick="submitForm()" style="width:180px;height:30px" iconCls="icon-user">登 录</a></p>
    </form>
</div>
</div>
</body>
</html>
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
<title>日志输出面板</title>
<link rel="stylesheet" type="text/css" href="css/common.css">
<script type="text/javascript" src="${ctx}/js/jquery.min.js"></script>
<script type="text/javascript">var contextPath="${ctx}";</script>
<style type="text/css">
#container{
    width:95%;
    height:650px;
    padding:10px;
    background-color: #000;
    color:#fff;
    font-size:12px;
    overflow: auto;
    word-wrap:break-word
}
#bar{color:red}
</style>
</head>
<body>
<div style="padding:10px">
	<div><button onclick="tail(0)" id="btn_rt">实时日志</button>&nbsp;&nbsp;&nbsp;
	<input type="number" id="line" maxlength="4" style="width:100px" max="3000" placeholder="查看前n行">
	<button onclick="tail(1)" id="btn_line">确定</button>
	</div>
	<div id="bar"><span>状态: </span><span id="statusbar">正在连接</span></div>
</div>
<div id="container">
</div>
<script type="text/javascript">
   var ws;
   $(function(){
        ws = new WebSocket('ws://<%=basePath%>/realTimeLogViewer?serverId=${param.ip}&port=${param.port}');
        ws.onopen = function()
        {
        	modstatus("已连接");
        };
        ws.onclose = function(evt)
        {
        	modstatus("已关闭."+evt.data?evt.data:"");
        };
        ws.onmessage = function(event) {
        	var $c=$('#container');
            $c.append("<p>"+event.data+"</p>");
            console.log($c.prop('scrollHeight'));
            // 滚动条滚动到最低部
            $c.scrollTop($c.prop('scrollHeight'));
        };
        ws.onerror = function(evt) { 
        	modstatus(evt.data);
        }; 
    });
 
  function modstatus(text){
    $('#statusbar').html(text);
  }
  
  function tail(line){
	  var cmd="cmd=tail";
	  if(line==1){
		  var n=$('#line').val();
		  cmd=cmd+"&line="+(n==""?500:$.trim(n));
	  }
	 // console.log(cmd);
	  $('#container').empty();
	  sendCmd(cmd);
	  if(line==0){
		  $('#btn_rt').prop('disabled','disabled');
	  }
  }
  
  function sendCmd(cmd){
	  if(ws.readyState==1){
		  ws.send(cmd);
	  }else{
		  modstatus("连接状态"+ws.readyState);
	  }
  }

</script>
</body>
</html>
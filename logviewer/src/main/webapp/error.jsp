<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>500 - 系统错误</title>
<style>
body, p, div {
    padding: 0;
    margin: 0;
    color: #333;
}

body {
    background-color: #f8f8f8;
    font-family:Helvetica, Tahoma, Arial;
    text-align: center;
}
.header {
    font-size: 64px;
    border-bottom: solid 1px #999;
    margin-bottom:30px;
    padding:15px;
}
.wrapper {
    width: 550px;
    margin: 100px auto;
}
a{color:red}
.cont{padding-bottom:15px;font-size: 14px;}
</style>
</head>
<body>
	 <div class="wrapper">
        <p class="header">500 ERROR</p>
        <p class="cont">
       系统错误! 请联系管理员或尝试重复操作</p>
        <%out.println(exception.getMessage()); %>
    </div>
</body>
</html>

<%@page import="org.jflame.logviewer.util.Config"%>
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
<title>日志查看系统</title>
<link rel="stylesheet" type="text/css" href="css/common.css">
<link rel="stylesheet" type="text/css" href="${ctx}/js/plugins/easyui/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" href="${ctx}/js/plugins/easyui/themes/icon.css">
<script type="text/javascript" src="${ctx}/js/jquery.min.js"></script>
<script type="text/javascript" src="${ctx}/js/plugins/easyui/jquery.easyui.min.js"></script>
<script type="text/javascript" src="${ctx}/js/plugins/easyui/locale/easyui-lang-zh_CN.js"></script>
<script type="text/javascript">var contextPath="${ctx}";</script>
<style type="text/css">
#leftPanel{width:180px}
.nav{padding:15px 0px;}
.nav li{list-style-type: none;line-height:24px;}
.nav li a{text-decoration: none;font-size:12px;color:#333}
#headPanel {color:#fff;font-size:20px;padding-left:20px}
</style>
</head>
<body class="easyui-layout">
    <div id="headPanel" data-options="region:'north',border:false,split:true" 
    style="overflow:hidden;height:40px;background-color:#27598A;">
        日志查看系统
        <a href="${ctx}/logout" style="display: inline-block;color:#fff;float: right;font-size:16px;padding-right:20px">退出</a>
    </div>
 <!-- begin of sidebar -->
    <div id="leftPanel" data-options="region:'west',split:true,border:true,title:'导航'"> 
        <div class="easyui-accordion" data-options="border:false,fit:true"> 
            <div title="项目日志">
	            <ul class="nav easyui-tree">
	            <%request.setAttribute("projs", Config.PROJ_INFOS); 
	            request.setAttribute("servers", Config.SERVER_INFOS);
	            %>
	            <c:if test="${not empty projs}">
	            <c:forEach items="${projs}" var="item">
	            <li iconCls="icon-detail"><a href="javascript:void(0)" data-title="${item.projName}日志文件" data-link="${ctx}/logFileView?projId=${item.projId}">${item.projName}</a></li>
	            </c:forEach>
	            </c:if>
	            </ul>
            </div>
            <div title="服务器实时日志">
                <ul class="nav easyui-tree">
                <c:if test="${not empty servers}">
                 <c:forEach items="${servers}" var="item">
                <li iconCls="icon-layout"><a href="javascript:void(0)" data-title="${item.ip}实时日志" title="${item.desc}" data-link="realTimeLogViewer.jsp?ip=${item.ip}">${item.ip}</a></li>
                </c:forEach>
                </c:if>
                </ul>
            </div>
        </div>
    </div>  
    <!-- end of sidebar -->    
    <!-- begin of main -->
    <div id="mainPanel" data-options="region:'center'">
        <div id="mainTab" class="easyui-tabs" data-options="border:false,fit:true,tools:'#tab-tools',onContextMenu:'contextMenu'">  
        </div>
    </div>
    <!-- tabs右键菜单 -->
      <div id="tabContextMenu" class="easyui-menu" style="width:150px;">
        <div id="mm-tabclose" data-options="name:1">关闭</div>
        <div id="mm-tabcloseall" data-options="name:2">全部关闭</div>
        <div id="mm-tabcloseother" data-options="name:3">除此之外全部关闭</div>
    </div>
    <script type="text/javascript">
    $(function(){ 
	     $('.nav').on("click","a",function(){
	         var clickLink=$(this);
	         var title;
	         if(clickLink.attr('data-title')!=''){
	        	 title=clickLink.attr('data-title');
	         }else{
	        	 title = clickLink.text();
	         }
	         var url = clickLink.attr('data-link');
	         if(url!="")
	             addTab(title,url,true);
	     }); 
        $('#mainTab').tabs({
            onContextMenu:function(e, title,index){
                e.preventDefault();
                if(index>0){
                    $('#tabContextMenu').menu('show', {
                        left: e.pageX,
                        top: e.pageY
                    }).data("tabTitle", title);
                }
            }
        });
        //右键菜单click
        $("#tabContextMenu").menu({
            onClick : function (item) {
                removeTab(this, item.name);
            }
        });    
    });

    /**
     * 添加菜单选项
     * param title 名称
     * param href 链接
     * param iframe 链接跳转方式（true为iframe，false为href）
     */  
     function addTab(title, href, iframe){
         var tabPanel = $('#mainTab');
         if(!tabPanel.tabs('exists',title)){
             var content = '<iframe frameborder="0" src="'+ href +'" style="width:100%;height:100%;"></iframe>';
             if(iframe){
                 tabPanel.tabs('add',{
                     title:title,
                     content:content,
                     fit:true,
                     closable:true
                 });
             }
             else{
                 tabPanel.tabs('add',{
                     title:title,
                     href:href,
                     fit:true,
                     closable:true
                 });
             }
         }
         else
         {
             tabPanel.tabs('select',title);
             var selectedTab=tabPanel.tabs('getSelected');
             if(selectedTab){
                 var contentIframes = selectedTab.find('iframe');    
                 if(contentIframes&&contentIframes.length>0){
                     contentIframes[0].src=href;
                 }
             }
         }
     }
    
     //移除菜单选项卡
     function removeTabBtn(){
         var tabPanel = $('#mainTab');
         var tab = tabPanel.tabs('getSelected');
         if (tab){
             var index = tabPanel.tabs('getTabIndex', tab);
             tabPanel.tabs('close', index);
         }
     }
      //右键菜单删除选项卡
     function removeTab(menu, type){
        var tabPanel = $('#mainTab');
        var allTabtitle = [];
        var curTabTitle = $(menu).data("tabTitle");
        if(type==1){
            tabPanel.tabs("close", curTabTitle);
        }else{
             $.each(tabPanel.tabs('tabs'),function(i,n){
                 var opt=$(n).panel('options');
                 if(opt.closable)
                     allTabtitle.push(opt.title);
             });
             if(type==2){
                 for(var i=0;i<allTabtitle.length;i++){
                     tabPanel.tabs('close', allTabtitle[i]);
                 }
             }else if(type==3){
                 for(var i=0;i<allTabtitle.length;i++){
                     if(curTabTitle!=allTabtitle[i])
                           tabPanel.tabs('close', allTabtitle[i]);
                 }
             }
        }
     }
    
    
    //更新选项卡(新标题,新url,是否关闭同名的旧选项卡)
    function updateTabTitle(newtitle,newurl,isCloseOldTab){
         var tabPanel = $('#mainTab');
         if(isCloseOldTab&&tabPanel.tabs('exists',newtitle)){
             tabPanel.tabs('close',newtitle)
         }
         var seltab = tabPanel.tabs('getSelected');//当前标签页
         tabPanel.tabs('update', {
             tab: seltab,
             options: {
                title: newtitle,
             }}
         );
         var contentIframes = seltab.find('iframe');
         if (contentIframes && contentIframes.length > 0) {
             contentIframes[0].src = newurl;
         }
    }
    function closeRepeatTab(title){
        var tabPanel = $('#mainTab');
        if(tabPanel.tabs('exists',title)){
            tabPanel.tabs('close',title);
        }
    }
</script>
</body>
</html>
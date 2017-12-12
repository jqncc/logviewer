/* 通用js */
//ajax标记(部分浏览器没有所以此处统一加入)
$.ajaxSetup({"x-requested-with":"XMLHttpRequest"});
$(document).ajaxError(function(event,xhr,options){
	if(xhr.status==401){
		alert("未登录或登录已失效,请重新登录.");
		top.location.href=top.location.href;
	}
	else if(xhr.status==403){
		alert("对不起,您没有操作权限.");
		return;
	}else{
		//console.log(xhr.status+"==="+xhr.responseText);
		if(xhr.responseText&&xhr.responseText.length>2){
			try{
			   var callResult=$.parseJSON(xhr.responseText);
			   alert(callResult.message);
			}catch(err){
				console.error(err);
			}
		}else{
			alert("执行错误.");
		}
	}
});


/**
 * 分页datagrid数据转换
 * @param result
 * @returns {___anonymous128_171}
 */
function dataGridFilter(result){
	if(result && result.status==200){
		var page = result.data;
		if(page&&page.records){
			return {total:page.total,rows:page.records};
		}else{
			return {total:0,rows:null};
		}
	}else{
		showWarn("无加载数据");
	}
}
/**
 * 无分页datagrid数据转换
 * @param result
 * @returns
 */
function dataGridNoPagerFilter(result){
	if(result && result.status==200){
		if(result.data!=null)
		  return result.data;
		else{
		  return [];
		}
	}else{
		showWarn("查询数据错误");
	}
}
/**
 * 请求正确返回CallResult结果处理抽象
 * @param result CallResult
 * @param callback 成功回调方法
 */
function doSuccess(result,callback){
	if(result && result.status==200){
		callback(result.data);
	}else{
		showError(result.message);
	}
}

/**
 * 显示普通消息
 * @param message
 */
function showMsg(message){
	$.messager.alert('消息',message);
}
/**
 * 显示错误消息
 * @param message
 */
function showError(message){
	$.messager.alert('错误',message,'error');
}
/**
 * 显示提示消息
 * @param message
 */
function showInfo(message){
	$.messager.alert('提示',message,'info');
}
/**
 * 显示提示消息
 * @param message 消息
 * @param top 设置面板距离顶部的位置（即Y轴位置）。如果isPercent为true,则该值为小数
 * @param isPercent 是否为百分比
 */
function showInfo(message,top,isPercent){
	//$(window).height()为浏览器当前窗口可视区域高度
	var relTop = isPercent == true ? ($(window).height()) * top : top;
	$.messager.alert({
		title : '提示',
		msg : message,
		icon : 'info',
		top : relTop
	});
}
/**
 * 显示警告消息
 * @param message
 */
function showWarn(message){
	$.messager.alert('警告',message,'warning');
}
/**
 * 显示警告消息
 * @param message 消息
 * @param top 设置面板距离顶部的位置（即Y轴位置）。如果isPercent为true,则该值为小数
 * @param isPercent 是否为百分比
 */
function showWarn(message,top,isPercent){
	//$(window).height()为浏览器当前窗口可视区域高度
	var relTop = isPercent == true ? ($(window).height()) * top : top;
	$.messager.alert({
		title : '警告',
		msg : message,
		icon : 'warning',
		top : relTop
	});
}
//数组是否有重复元素
function isRepeatArray(arr) {
   var hash = {};
   for(var i in arr) {
       if(hash[arr[i]]==1)
       {
           return true;
       }
       hash[arr[i]] = 1;
    }
   return false;
}
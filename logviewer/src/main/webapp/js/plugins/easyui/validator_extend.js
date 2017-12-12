/**
 * 扩展validatebox验证规则.
 * 内置规则:
 *  email：匹配 email 正则表达式规则。
 *  url：匹配 URL 正则表达式规则。
 *  length[0,100]：允许从 x 到 y 个字符。
 *  remote['http://.../action.do','paramName']：发送 ajax 请求来验证值，成功时返回 'true' 。
 * 新增规则:
 *   combox选择框必填.
 *   regex正则验证.
 *   equals两元素比较.
 *   minLength最小长度.
 *   maxLength最大长度.
 *   phone电话格式.
 *   mobile手机格式.
 *   intOrFloat整数或小数
 *   currency货币.
 *   integer整数.
 *   chinese中文.
 *   english英文字母.
 *   unnormal是否包含空格或非法字符.
 *   idcard中国身份证.
 *   ip ip地址.
 *   zip邮政编码
 *   letterOrNumOrline由字母、数字或下划线组成，长度{0}-{1}字符.
 */
$.extend($.fn.validatebox.defaults.rules, {
	combox : {  
        validator : function(value, param,missingMessage) {  
            if($('#'+param).combobox('getValue')!='' && $('#'+param).combobox('getValue')!=null){  
                return true;  
            }  
            return false;  
        },  
        message : "{1}"  
    },
    regex: {   
        validator: function(value, param){
        	var re = new RegExp(param[0]); 
            return re.test(value);   
        },   
        message: '值不符合要求'  
    }, 
    equals : {
        validator : function(value, param) {
            return value == $(param[0]).val();
        },
        message : '值不相等'
    },
    minLength: {
        validator: function(value, param){
            return value.length >= param[0];
        },
        message: '至少{0}个字符.'
    },
    maxLength: {
        validator: function(value, param){
            return value.length <= param[0];
        },
        message: '最多{0}个字符.'
    },
    phone : {// 验证电话号码
        validator : function(value) {
            return /^((\(\d{2,3}\))|(\d{3}\-))?(\(0\d{2,3}\)|0\d{2,3}-)?[1-9]\d{6,7}(\-\d{1,4})?$/i.test(value);
        },
        message : '格式不正确,请使用下面格式:010-88888888'
    },
    mobile : {// 验证手机号码
        validator : function(value) {
            return isMobile(value);
        },
        message : '手机号码格式不正确'
    },
    intOrFloat : {// 验证整数或小数
        validator : function(value) {
            return /^\d+(\.\d+)?$/i.test(value);
        },
        message : '请输入数字，并确保格式正确'
    },
    currency : {// 验证货币
        validator : function(value) {
            return /^\d+(\.\d+)?$/i.test(value);
        },
        message : '货币格式不正确'
    },
    integer : {// 验证整数
        validator : function(value) {
            return /^[+]?[0-9]+\d*$/i.test(value);
        },
        message : '请输入整数'
    },
    chinese : {// 验证中文
        validator : function(value) {
            return /^[\Α-\￥]+$/i.test(value);
        },
        message : '请输入中文'
    },
    english : {// 验证英语
        validator : function(value) {
            return /^[A-Za-z]+$/i.test(value);
        },
        message : '请输入英文'
    },
    unnormal : {// 验证是否包含空格和非法字符
        validator : function(value) {
            return /.+/i.test(value);
        },
        message : '输入值不能为空和包含其他非法字符'
    },
    letterOrNumOrline : {
        validator : function(value,param) {
        	return /^[a-zA-Z0-9_]+$/i.test(value)&&value.length>=param[0]&&value.length<=param[1];
        },
        message : '由字母、数字或下划线组成，长度{0}-{1}字符'
    },
    zip : {// 验证邮政编码
        validator : function(value) {
            return /^[0-9]\d{5}$/i.test(value);
        },
        message : '邮政编码格式不正确'
    },
    ip : {// 验证IP地址
        validator : function(value) {
            return isIpv4(value)||isIpv6(value);
        },
        message : 'IP地址格式不正确'
    },
    idcard : {// 验证身份证
            validator : function(value) {
                return isIDcard(value);
            },
            message : '不是有效的身份证'
    },
    safeChar:{
    	validator : function(value) {
            return safeChar(value);
        },
        message : '包含特殊符号'
    },
    date: {
		validator: function(value, param){
			var dateValue = $.fn.datebox.defaults.parser(value);
			var start=null,end=null;
			if(param[0]!=''){
				if(param[0].charAt(0)=='#'){
					var tmpStart=$(param[0]).datebox('getValue')
					if(tmpStart!='')
						start=$.fn.datebox.defaults.parser(tmpStart);
				}else{
					start=$.fn.datebox.defaults.parser(param[0]);
				}
			}
			if(param[1]!=''){
				if(param[1].charAt(0)=='#'){
					var tmpEnd=$(param[1]).datebox('getValue')
					if(tmpEnd!='')
						end=$.fn.datebox.defaults.parser(tmpEnd);
				}else{
					end=$.fn.datebox.defaults.parser(param[1]);
				}
			}
			if(start!=null&&end!=null){
				return start<=dateValue&&end>=dateValue;
			}else if(start!=null&&end==null){
				return start<=dateValue;
			}else if(start==null&&end!=null){
				return end>=dateValue;
			}
			return true;
		},
		message: '时间不正确.'
	}
});
 

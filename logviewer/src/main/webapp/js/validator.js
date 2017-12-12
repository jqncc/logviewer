/**
 * 常用验证方法
 */
/**
 * 验证中国身份证
 */
function isIDcard(idcard){
	var Wi = [ 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1 ];// 加权因子;
	var ValideCode = [ 1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2 ];// 身份证验证位值，10代表X;
	if (idcard.length == 15) {   
		return isValidityBrithBy15IdCard(idcard);   
	}else if (idcard.length == 18){   
		var a_idCard = idcard.split("");// 得到身份证数组   
		if (isValidityBrithBy18IdCard(idcard)&&isTrueValidateCodeBy18IdCard(a_idCard)) {   
			return true;   
		}   
		return false;
	}

	return false;
	
	function isTrueValidateCodeBy18IdCard(a_idCard) {   
		var sum = 0; // 声明加权求和变量   
		if (a_idCard[17].toLowerCase() == 'x') {   
			a_idCard[17] = 10;// 将最后位为x的验证码替换为10方便后续操作   
		}   
		for ( var i = 0; i < 17; i++) {   
			sum += Wi[i] * a_idCard[i];// 加权求和   
		}   
		valCodePosition = sum % 11;// 得到验证码所位置   
		if (a_idCard[17] == ValideCode[valCodePosition]) {   
			return true;   
		}
		return false;   
	}
	
	function isValidityBrithBy18IdCard(idCard18){   
		var year = idCard18.substring(6,10);   
		var month = idCard18.substring(10,12);   
		var day = idCard18.substring(12,14);   
		var temp_date = new Date(year,parseFloat(month)-1,parseFloat(day));   
		// 这里用getFullYear()获取年份，避免千年虫问题   
		if(temp_date.getFullYear()!=parseFloat(year) || temp_date.getMonth()!=parseFloat(month)-1 || temp_date.getDate()!=parseFloat(day)){   
			return false;   
		}
		return true;   
	}
	
	function isValidityBrithBy15IdCard(idCard15){   
		var year =  idCard15.substring(6,8);   
		var month = idCard15.substring(8,10);   
		var day = idCard15.substring(10,12);
		var temp_date = new Date(year,parseFloat(month)-1,parseFloat(day));   
		// 对于老身份证中的你年龄则不需考虑千年虫问题而使用getYear()方法   
		if(temp_date.getYear()!=parseFloat(year) || temp_date.getMonth()!=parseFloat(month)-1 || temp_date.getDate()!=parseFloat(day)){   
			return false;   
		}
		return true;
	}
}
/**
 * ip v4验证
 * @param {} ip
 * @return {}
 */
function isIpv4(ip){
    var reg = /^((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\.){3}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})$/;  
    return reg.test(ip);  
}
/**
 * ip v6验证
 * @param {} ip
 * @return {}
 */
function isIpv6(ip){
    var reg = /^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/;  
    return reg.test(ip);  
}
/**
 * 验证手机号
 * @return {}
 */
function isMobile(value){
    return /^[1][3|5|7|8]\d{9}$/.test(value);
}
/**
 * 验证电话号码
 * @param {} value
 * @return {}
 */
function isPhone(value){
    return /^((\(\d{2,3}\))|(\d{3}\-))?(\(0\d{2,3}\)|0\d{2,3}-)?[1-9]\d{6,7}(\-\d{1,4})?$/i.test(value);
}
/**
 * 验证email
 * @param {} emailValue
 * @return {}
 */
function isEmail(emailValue){  
    var reg = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;  
    return reg.test(emailValue);  
}
/**
 * 验证整数，正或负^\\w+$
 */
function isInt(num){
	return /^-?\d+$/.test(num);
}
/**
 * 验证数字，整数或小数
 */
function isNumber(num){
	return /^(-?\d+[\s,]*)+([.]{1}[0-9]+){0,1}$/.test(num);
}
/**
 * 验证小数
 * @param {} num
 * @return {}
 */
function isFloat(num)     
{     
    return /^(-?\d+)(\.\d+)?$/.test(num);   
}     
/**
 * 验证字符串是否为空
 * @param {} str
 * @return {}
 */
function isEmpty(str){
	return !str||str.length==0;
}
/**
 * 验证url
 * @param {} url
 * @return {}
 */
function isUrl(url){
    return /^[a-zA-z]+:\/\/(\w+(-\w+)*)(\.(\w+(-\w+)*))*(\?\S*)?$/i.test(url);
}
/**
 * 验证http url
 * @param {} url
 * @return {}
 */
function isHttpUrl(url){
	return /^https?:\/\/(\w+(-\w+)*)(\.(\w+(-\w+)*))*(\?\S*)?$/i.test(url);
}
/**
 * 验证是否是字母、数字或下划线
 * @param {} str
 * @return {}
 */
function isLetterOrNumOrUnderline(str){
    return /^\w+$/.test(str);
}
function safeChar(value){
  var re = /[~#^$><%&!*=`-]/gi;  
  return !re.test(value); 
}
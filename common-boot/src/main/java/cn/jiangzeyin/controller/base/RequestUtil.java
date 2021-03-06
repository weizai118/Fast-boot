package cn.jiangzeyin.controller.base;

import cn.jiangzeyin.StringUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求工具类
 *
 * @author jiangzeyin
 */
public final class RequestUtil {
    /**
     * 更加url 获取get 参数
     *
     * @param url url
     * @return map
     * @throws UnsupportedEncodingException 编码异常
     */
    public static Map<String, String> convertUrlMap(String url) throws UnsupportedEncodingException {
        if (StringUtil.isEmpty(url)) {
            return null;
        }
        Map<String, String> mapRequest = new HashMap<>();
        url = url.trim().toLowerCase();
        String[] arrSplit = url.split("[?]");
        if (arrSplit.length <= 1) {
            return mapRequest;
        }
        String allParam = arrSplit[1];
        if (StringUtil.isEmpty(allParam)) {
            return mapRequest;
        }
        arrSplit = allParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = strSplit.split("[=]");
            //解析出键值
            if (arrSplitEqual.length > 1) {
                //正确解析
                mapRequest.put(arrSplitEqual[0], URLDecoder.decode(arrSplitEqual[1], "UTF-8"));
            } else {
                if (!"".equals(arrSplitEqual[0])) {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }


    /**
     * @param request req
     * @param name    name
     * @return cookie
     * @author jiangzeyin
     */
    public static Cookie getCookieByName(HttpServletRequest request, String name) {
        Map<String, Cookie> cookieMap = readCookieMap(request);
        return cookieMap.getOrDefault(name, null);
    }

    /**
     * @param request req
     * @return map
     * @author jiangzeyin
     */
    private static Map<String, Cookie> readCookieMap(HttpServletRequest request) {
        Map<String, Cookie> cookieMap = new HashMap<>();
        Cookie[] cookies = request.getCookies();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie);
            }
        }
        return cookieMap;
    }

    /**
     * 获取headr
     *
     * @param request req
     * @return map
     * @author jiangzeyin
     */
    public static Map<String, String> getHeaderMapValues(HttpServletRequest request) {
        Enumeration<String> enumeration = request.getHeaderNames();
        Map<String, String> headerMapValues = new HashMap<>();
        if (enumeration != null) {
            for (; enumeration.hasMoreElements(); ) {
                String name = enumeration.nextElement();
                headerMapValues.put(name, request.getHeader(name));
            }
        }
        return headerMapValues;
    }
}

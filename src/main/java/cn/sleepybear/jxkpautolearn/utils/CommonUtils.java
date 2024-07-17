package cn.sleepybear.jxkpautolearn.utils;

import okhttp3.Cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2024/07/16 19:11
 */
public class CommonUtils {
    public static void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public static List<Cookie> parseCookies(String cookieStr, String domain) {
        List<Cookie> cookies = new ArrayList<>();
        String[] pairs = cookieStr.split(";");

        for (String pair : pairs) {
            String[] keyValue = pair.trim().split("=");
            if (keyValue.length == 2) {
                String name = keyValue[0].trim();
                String value = keyValue[1].trim();
                Cookie cookie = new Cookie.Builder()
                        .name(name)
                        .value(value)
                        .domain(domain)
                        .build();
                cookies.add(cookie);
            }
        }
        return cookies;
    }
}

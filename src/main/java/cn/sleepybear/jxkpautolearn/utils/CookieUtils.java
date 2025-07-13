package cn.sleepybear.jxkpautolearn.utils;

import cn.sleepybear.cacher.Cacher;
import cn.sleepybear.cacher.CacherBuilder;
import cn.sleepybear.jxkpautolearn.dto.UserInfoDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2024/05/01 22:55
 */
public class CookieUtils {
    public static final Cacher<String, UserInfoDto> USER_CACHER = new CacherBuilder<String, UserInfoDto>().scheduleName("USER_CACHER").delay(30, TimeUnit.SECONDS).build();
    public static final Cacher<String, Object> ADMIN_CACHER = new CacherBuilder<String, Object>().scheduleName("ADMIN_CACHER").delay(30, TimeUnit.SECONDS).build();

    public static final String COOKIE_NAME = "jxkp-auto-learn-user-cookie";
    public static final int COOKIE_MAX_AGE = Integer.MAX_VALUE;

    public static String buildCookieStr(String name, String idCard) {
        return name + "@" + idCard;
    }

    public static Cookie getCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if (COOKIE_NAME.equals(name)) {
                return cookie;
            }
        }

        return null;
    }

    public static void setWebUserCookie(HttpServletResponse response, String cookieValue, int expireTime) {
        if (cookieValue == null || response == null) {
            return;
        }

        Cookie cookie = new Cookie(COOKIE_NAME, cookieValue);
        cookie.setPath("/");
        cookie.setMaxAge(expireTime);
        response.addCookie(cookie);
    }

    public static void setWebUserCookie(String cookieValue, int expireTime) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return;
        }
        HttpServletResponse response = ((ServletRequestAttributes) requestAttributes).getResponse();
        setWebUserCookie(response, cookieValue, expireTime);

    }

    public static void setWebUserCookie(String userCookie) {
        String s = "";
        if (StringUtils.isNotBlank(userCookie)) {
            s += userCookie;
        }
        setWebUserCookie(s, COOKIE_MAX_AGE);
    }

    public static void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = getCookie(request);
        if (cookie != null) {
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }
}

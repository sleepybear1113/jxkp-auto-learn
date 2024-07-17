package cn.sleepybear.jxkpautolearn.utils;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * There is description
 * @author sleepybear
 * @date 2023/12/11 00:53
 */
public class MyCookieJar implements Serializable, CookieJar {
    @Serial
    private static final long serialVersionUID = 2509512576422910138L;

    private final Map<String, Cookie> cookies = new HashMap<>();

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        return getCookieList();
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
        for (Cookie cookie : list) {
            cookies.put(cookie.name(), cookie);
        }

        clearExpiredCookies();
    }

    public void clearExpiredCookies() {
        cookies.entrySet().removeIf(entry -> entry.getValue() != null && entry.getValue().expiresAt() < System.currentTimeMillis());
    }

    public List<Cookie> getCookieList() {
        clearExpiredCookies();
        return new ArrayList<>(cookies.values());
    }

    public static List<Cookie> getCookieList(CookieJar cookieJar) {
        if (cookieJar instanceof MyCookieJar) {
            return ((MyCookieJar) cookieJar).getCookieList();
        }

        return new ArrayList<>();
    }

    public void addCookie(Cookie cookie) {
        cookies.put(cookie.name(), cookie);
    }

    public void addCookies(List<Cookie> cookieList) {
        for (Cookie cookie : cookieList) {
            cookies.put(cookie.name(), cookie);
        }
    }
}

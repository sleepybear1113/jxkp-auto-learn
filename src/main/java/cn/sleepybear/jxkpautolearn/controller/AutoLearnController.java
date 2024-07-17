package cn.sleepybear.jxkpautolearn.controller;

import cn.sleepybear.cacher.cache.ExpireWayEnum;
import cn.sleepybear.jxkpautolearn.config.GlobalConstants;
import cn.sleepybear.jxkpautolearn.dto.CourseInfoDto;
import cn.sleepybear.jxkpautolearn.dto.UserInfoDto;
import cn.sleepybear.jxkpautolearn.exception.FrontException;
import cn.sleepybear.jxkpautolearn.logic.AutoLearnLogic;
import cn.sleepybear.jxkpautolearn.utils.CommonUtils;
import cn.sleepybear.jxkpautolearn.utils.CookieUtils;
import cn.sleepybear.jxkpautolearn.utils.MyCookieJar;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.Cookie;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2024/07/17 00:46
 */
@RestController
@RequestMapping(value = GlobalConstants.PREFIX)
public class AutoLearnController {

    @RequestMapping("/addUserCookie")
    public UserInfoDto addUserCookie(String cookie) {
        if (StringUtils.isBlank(cookie)) {
            throw new FrontException("Cookie 信息不能为空");
        }

        if (!cookie.contains(".AspNetCore.Cookies")) {
            throw new FrontException("Cookie 信息格式不正确");
        }

        List<Cookie> cookies = CommonUtils.parseCookies(cookie, AutoLearnLogic.DOMAIN);
        if (CollectionUtils.isEmpty(cookies)) {
            throw new FrontException("Cookie 信息解析失败");
        }

        UserInfoDto userInfoDto = new UserInfoDto();
        MyCookieJar myCookieJar = new MyCookieJar();
        myCookieJar.addCookies(cookies);
        userInfoDto.setMyCookieJar(myCookieJar);

        boolean userProfile = AutoLearnLogic.getUserProfile(userInfoDto);
        if (!userProfile) {
            throw new FrontException("无法获取用户信息");
        }

        CookieUtils.USER_CACHER.put(userInfoDto.getKey(), userInfoDto, 300L * 1000, ExpireWayEnum.AFTER_ACCESS);
        CookieUtils.setWebUserCookie(userInfoDto.getKey(), CookieUtils.COOKIE_MAX_AGE);
        return userInfoDto;
    }

    @RequestMapping("/getUserProfile")
    public UserInfoDto getUserProfile(HttpServletRequest request) {
        UserInfoDto userInfoDto = a(request);
        AutoLearnLogic.getUserProfile(userInfoDto);
        return userInfoDto;
    }

    @RequestMapping("/getPersonTotalLessons")
    public static List<CourseInfoDto> getPersonTotalLessons(HttpServletRequest request) {
        UserInfoDto userInfoDto = a(request);
        return AutoLearnLogic.getPersonTotalLessons(userInfoDto);
    }

    @RequestMapping("/learn")
    public static void learn(HttpServletRequest request, String[] kcIdList) {
        if (kcIdList == null || kcIdList.length == 0) {
            throw new FrontException("课程 ID 不能为空");
        }

        UserInfoDto userInfoDto = a(request);
        List<String> kcIdList1 = List.of(kcIdList);
        userInfoDto.setLessonKcIdList(kcIdList1);

        Thread.ofVirtual().start(() -> AutoLearnLogic.learn(userInfoDto));
    }

    @RequestMapping("/stop")
    public static void stop(HttpServletRequest request) {
        UserInfoDto userInfoDto = a(request);
        userInfoDto.setStopping(true);
    }

    @RequestMapping("/getCaptchaImg")
    public static void getCaptchaImg(HttpServletRequest request, HttpServletResponse response, String idCard, String password) {
        if (StringUtils.isBlank(idCard) || StringUtils.isBlank(password)) {
            throw new FrontException("账号和或密码不能为空");
        }

        UserInfoDto userInfoDto = CookieUtils.USER_CACHER.get(idCard);
        if (userInfoDto == null) {
            userInfoDto = new UserInfoDto();
            userInfoDto.setIdCard(idCard);
        }

        userInfoDto.setMyCookieJar(new MyCookieJar());
        userInfoDto.setPassword(password);
        AutoLearnLogic.getHomePage(userInfoDto);

        AutoLearnLogic.getCaptchaToken(response, userInfoDto);

        CookieUtils.USER_CACHER.put(userInfoDto.getKey(), userInfoDto, 300L * 1000, ExpireWayEnum.AFTER_ACCESS);
    }

    @RequestMapping("/login")
    public UserInfoDto login(String idCard, String password, String captcha) {
        if (StringUtils.isBlank(idCard) || StringUtils.isBlank(password) || StringUtils.isBlank(captcha)) {
            throw new FrontException("账号、密码和或验证码不能为空");
        }

        UserInfoDto userInfoDto = CookieUtils.USER_CACHER.get(idCard);
        if (userInfoDto == null) {
            throw new FrontException("请先获取验证码");
        }

        userInfoDto.setIdCard(idCard);
        userInfoDto.setPassword(password);
        AutoLearnLogic.login(userInfoDto, captcha);
        return userInfoDto;
    }

    private static UserInfoDto a(HttpServletRequest request) {
        jakarta.servlet.http.Cookie cookie = CookieUtils.getCookie(request);
        if (cookie == null) {
            throw new FrontException("请先添加 Cookie");
        }

        String key = cookie.getValue();
        if (StringUtils.isBlank(key)) {
            throw new FrontException("Cookie 信息为空");
        }

        UserInfoDto userInfoDto = CookieUtils.USER_CACHER.get(key);
        if (userInfoDto == null) {
            throw new FrontException("Cookie 信息已过期，请重新添加");
        }

        return userInfoDto;
    }
}

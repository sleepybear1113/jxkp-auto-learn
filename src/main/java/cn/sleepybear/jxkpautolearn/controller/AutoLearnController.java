package cn.sleepybear.jxkpautolearn.controller;

import cn.sleepybear.cacher.cache.ExpireWayEnum;
import cn.sleepybear.jxkpautolearn.advice.ResultCodeConstant;
import cn.sleepybear.jxkpautolearn.config.AppConfig;
import cn.sleepybear.jxkpautolearn.config.GlobalConstants;
import cn.sleepybear.jxkpautolearn.dto.CourseInfoDto;
import cn.sleepybear.jxkpautolearn.dto.UserInfoDto;
import cn.sleepybear.jxkpautolearn.exception.FrontException;
import cn.sleepybear.jxkpautolearn.logic.AutoLearnLogic;
import cn.sleepybear.jxkpautolearn.service.UserWhitelistService;
import cn.sleepybear.jxkpautolearn.utils.CookieUtils;
import cn.sleepybear.jxkpautolearn.utils.MyCookieJar;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2024/07/17 00:46
 */
@RestController
@RequestMapping(value = GlobalConstants.PREFIX)
@RequiredArgsConstructor
public class AutoLearnController {

    private final AppConfig appConfig;
    private final UserWhitelistService userWhitelistService;

    @RequestMapping("/getUserProfile")
    public UserInfoDto getUserProfile(HttpServletRequest request) {
        UserInfoDto userInfoDto = a(request);
        AutoLearnLogic.getUserProfile(userInfoDto);
        
        // 检查用户是否在白名单中
        if (!userWhitelistService.isUserInWhitelist(userInfoDto)) {
            String userIdentifier = StringUtils.isNotBlank(userInfoDto.getName()) ? userInfoDto.getName() :
                                   StringUtils.isNotBlank(userInfoDto.getIdCard()) ? userInfoDto.getIdCard() :
                                   StringUtils.isNotBlank(userInfoDto.getTel()) ? userInfoDto.getTel() : "未知用户";
            
            // 执行退出登录操作
            logout(request);
            
            throw new FrontException("用户" + userIdentifier + "不在白名单内，请联系管理员添加");
        }
        
        return userInfoDto;
    }

    @RequestMapping("/getPersonTotalLessons")
    public static List<CourseInfoDto> getPersonTotalLessons(HttpServletRequest request) {
        UserInfoDto userInfoDto = a(request);
        return AutoLearnLogic.getPersonTotalLessons(userInfoDto);
    }

    @RequestMapping("/learn")
    public static void learn(HttpServletRequest request, String kcIdList) {
        if (StringUtils.isBlank(kcIdList)) {
            throw new FrontException("课程 ID 不能为空");
        }

        UserInfoDto userInfoDto = a(request);
        List<String> kcIdList1 = List.of(kcIdList.split(","));
        userInfoDto.setLessonKcIdList(kcIdList1);
        if (!Boolean.TRUE.equals(userInfoDto.getStop())) {
            throw new FrontException("当前有正在进行中的学习任务，无法重叠学习！");
        }
        Thread.ofVirtual().start(() -> AutoLearnLogic.learn(userInfoDto));
    }

    @RequestMapping("/stop")
    public static void stop(HttpServletRequest request) {
        UserInfoDto userInfoDto = a(request);
        userInfoDto.setStopping(true);
    }

    @RequestMapping("/forceDelete")
    public Boolean forceDelete(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            throw new FrontException("账号不能为空");
        }

        UserInfoDto userInfoDto = CookieUtils.USER_CACHER.get(idCard);
        if (userInfoDto == null) {
            throw new FrontException("账号不存在");
        }

        userInfoDto.setStop(true);
        userInfoDto.setStopping(true);
        CookieUtils.USER_CACHER.remove(idCard);
        return true;
    }

    @RequestMapping("/logout")
    public Boolean logout(HttpServletRequest request) {
        UserInfoDto userInfoDto = a(request);
        // 停止正在进行的自动学习
        userInfoDto.setStop(true);
        userInfoDto.setStopping(true);
        // 从缓存中删除用户信息
        CookieUtils.USER_CACHER.remove(userInfoDto.getKey());
        return true;
    }

    /**
     * 登录前置接口，获取验证码图片
     *
     * @param response HttpServletResponse
     * @param idCard   身份证号
     * @param password 密码
     */
    @RequestMapping("/getCaptchaImg")
    public void getCaptchaImg(HttpServletResponse response, String idCard, String password) {
        if (StringUtils.isBlank(idCard) || StringUtils.isBlank(password)) {
            throw new FrontException("账号和或密码不能为空");
        }

        UserInfoDto userInfoDto = CookieUtils.USER_CACHER.get(idCard);
        if (userInfoDto == null) {
            userInfoDto = new UserInfoDto();
            userInfoDto.setIdCard(idCard);

            // 判断是否是管理员
            if (appConfig.getAdminUsernameList().contains(idCard)) {
                userInfoDto.setAdmin(true);
            }
        }

        userInfoDto.setMyCookieJar(new MyCookieJar());
        userInfoDto.setPassword(password);

        // 获取首页，获取验证码 token
        AutoLearnLogic.getHomePage(userInfoDto);

        try {
            // 获取验证码图片，将 bytes 写入返回流，以图片形式
            byte[] bytes = AutoLearnLogic.getCaptchaToken(userInfoDto);
            response.setContentType("image/jpeg");
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new FrontException(ResultCodeConstant.CodeEnum.CANNOT_CONNECT_TO_SERVER, "无法连接至服务器！");
        }

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
        
        // 检查用户是否在白名单中
        if (!userWhitelistService.isUserInWhitelist(userInfoDto)) {
            String userIdentifier = StringUtils.isNotBlank(userInfoDto.getName()) ? userInfoDto.getName() :
                                   StringUtils.isNotBlank(userInfoDto.getIdCard()) ? userInfoDto.getIdCard() :
                                   StringUtils.isNotBlank(userInfoDto.getTel()) ? userInfoDto.getTel() : "未知用户";
            
            // 执行退出登录操作
            CookieUtils.USER_CACHER.remove(userInfoDto.getKey());
            
            throw new FrontException("用户" + userIdentifier + "不在白名单内，请联系管理员添加");
        }
        
        return userInfoDto;
    }

    @RequestMapping("/addUserCookie")
    public UserInfoDto addUserCookie(String cookie) {
        if (StringUtils.isBlank(cookie)) {
            throw new FrontException("Cookie不能为空");
        }

        // 生成一个唯一的用户标识
        String userKey = "cookie_user_" + System.currentTimeMillis();
        
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setIdCard(userKey);
        
        // 创建CookieJar并添加Cookie
        MyCookieJar myCookieJar = new MyCookieJar();
        Cookie.Builder cookieBuilder = new Cookie.Builder()
                .name(".AspNetCore.Cookies")
                .value(cookie)
                .domain("zy.jxkp.net")
                .path("/");
        myCookieJar.addCookie(cookieBuilder.build());
        userInfoDto.setMyCookieJar(myCookieJar);

        // 验证Cookie有效性
        boolean isValid = AutoLearnLogic.getUserProfile(userInfoDto);
        if (!isValid) {
            throw new FrontException("Cookie无效或已过期，请重新获取");
        }

        // 检查用户是否在白名单中
        if (!userWhitelistService.isUserInWhitelist(userInfoDto)) {
            String userIdentifier = StringUtils.isNotBlank(userInfoDto.getName()) ? userInfoDto.getName() :
                                   StringUtils.isNotBlank(userInfoDto.getIdCard()) ? userInfoDto.getIdCard() :
                                   StringUtils.isNotBlank(userInfoDto.getTel()) ? userInfoDto.getTel() : "未知用户";
            
            throw new FrontException("用户" + userIdentifier + "不在白名单内，请联系管理员添加");
        }

        // 缓存用户信息
        CookieUtils.USER_CACHER.put(userInfoDto.getKey(), userInfoDto, 300L * 1000, ExpireWayEnum.AFTER_ACCESS);
        CookieUtils.setWebUserCookie(userInfoDto.getKey(), CookieUtils.COOKIE_MAX_AGE);
        
        return userInfoDto;
    }

    private static UserInfoDto a(HttpServletRequest request) {
        jakarta.servlet.http.Cookie cookie = CookieUtils.getCookie(request);
        if (cookie == null) {
            throw new FrontException("请先添加 Cookie或者输入用户名登录");
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

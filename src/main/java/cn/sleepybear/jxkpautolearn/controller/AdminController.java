package cn.sleepybear.jxkpautolearn.controller;

import cn.sleepybear.cacher.cache.ExpireWayEnum;
import cn.sleepybear.jxkpautolearn.config.AppConfig;
import cn.sleepybear.jxkpautolearn.config.GlobalConstants;
import cn.sleepybear.jxkpautolearn.dto.UserInfoDto;
import cn.sleepybear.jxkpautolearn.exception.FrontException;
import cn.sleepybear.jxkpautolearn.service.UserWhitelistService;
import cn.sleepybear.jxkpautolearn.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员控制器
 *
 * @author sleepybear
 * @date 2025/01/20
 */
@RestController
@RequestMapping(value = GlobalConstants.PREFIX + "/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AppConfig appConfig;
    private final UserWhitelistService userWhitelistService;

    /**
     * 管理员登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @RequestMapping("/login")
    public UserInfoDto adminLogin(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new FrontException("用户名和密码不能为空");
        }

        // 验证管理员凭据
        boolean isValidAdmin = appConfig.getAdminUsers().stream().anyMatch(admin -> admin.getUsername().equals(username) && admin.getPassword().equals(password));

        if (!isValidAdmin) {
            throw new FrontException("管理员用户名或密码错误");
        }

        // 创建管理员用户信息
        UserInfoDto adminUser = new UserInfoDto();
        adminUser.setName(username);
        adminUser.setPassword(password);
        adminUser.setAdmin(true);

        // 缓存管理员信息
        String adminKey = "admin_" + username;
        CookieUtils.ADMIN_CACHER.put(adminKey, adminUser, 300L * 1000, ExpireWayEnum.AFTER_ACCESS);
        CookieUtils.setAdminCookie(adminKey, CookieUtils.COOKIE_MAX_AGE);

        return adminUser;
    }

    /**
     * 获取当前管理员信息
     *
     * @param request HttpServletRequest
     * @return 管理员信息
     */
    @RequestMapping("/getCurrentAdmin")
    public UserInfoDto getCurrentAdmin(HttpServletRequest request) {
        UserInfoDto userInfoDto = CookieUtils.getUserInfoDtoFromRequest(request, true);
        return CookieUtils.getUserInfoDtoFromRequest(request, true);
    }

    /**
     * 获取白名单用户列表
     *
     * @param request HttpServletRequest
     * @return 白名单用户列表
     */
    @RequestMapping("/getWhitelist")
    public List<String> getWhitelist(HttpServletRequest request) {
        UserInfoDto userInfoDto = CookieUtils.getUserInfoDtoFromRequest(request, true);
        return userWhitelistService.loadWhitelist();
    }

    /**
     * 添加用户到白名单
     *
     * @param users   用户列表，一行一个用户
     * @param request HttpServletRequest
     * @return 添加结果
     */
    @RequestMapping("/addUsers")
    public Integer addUsers(@RequestParam String users, HttpServletRequest request) {
        UserInfoDto userInfoDto = CookieUtils.getUserInfoDtoFromRequest(request, true);
        return userWhitelistService.addUsersToWhitelist(users);
    }

    /**
     * 从白名单中删除用户
     *
     * @param users   要删除的用户列表，用逗号分割
     * @param request HttpServletRequest
     * @return 删除结果
     */
    @RequestMapping("/removeUsers")
    public Integer removeUsers(@RequestParam("users") String users, HttpServletRequest request) {
        UserInfoDto userInfoDto = CookieUtils.getUserInfoDtoFromRequest(request, true);

        if (StringUtils.isBlank(users)) {
            throw new FrontException("要删除的用户列表不能为空");
        }

        List<String> userList = List.of(users.split(","));
        return userWhitelistService.removeUsersFromWhitelist(userList);
    }

    /**
     * 刷新白名单
     *
     * @param request HttpServletRequest
     * @return 刷新结果
     */
    @RequestMapping("/refreshWhitelist")
    public List<String> refreshWhitelist(HttpServletRequest request) {
        UserInfoDto userInfoDto = CookieUtils.getUserInfoDtoFromRequest(request, true);

        return userWhitelistService.loadWhitelist();
    }

    /**
     * 管理员退出登录
     *
     * @param request HttpServletRequest
     * @return 退出结果
     */
    @RequestMapping("/logout")
    public Boolean adminLogout(HttpServletRequest request) {
        UserInfoDto userInfoDto = CookieUtils.getUserInfoDtoFromRequest(request, true);

        // 从缓存中删除管理员信息
        String adminKey = "admin_" + userInfoDto.getName();
        CookieUtils.ADMIN_CACHER.remove(adminKey);
        CookieUtils.setAdminCookie(null, 0); // 清除管理员Cookie

        return true;
    }
} 
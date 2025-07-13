package cn.sleepybear.jxkpautolearn.controller;

import cn.sleepybear.jxkpautolearn.config.AppConfig;
import cn.sleepybear.jxkpautolearn.config.GlobalConstants;
import cn.sleepybear.jxkpautolearn.dto.AdminUserDto;
import cn.sleepybear.jxkpautolearn.exception.FrontException;
import cn.sleepybear.jxkpautolearn.service.UserWhitelistService;
import cn.sleepybear.jxkpautolearn.utils.CookieUtils;
import cn.sleepybear.cacher.cache.ExpireWayEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Map<String, Object> adminLogin(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new FrontException("用户名和密码不能为空");
        }

        // 验证管理员凭据
        boolean isValidAdmin = appConfig.getAdminUsers().stream()
                .anyMatch(admin -> admin.getUsername().equals(username) && admin.getPassword().equals(password));

        if (!isValidAdmin) {
            throw new FrontException("管理员用户名或密码错误");
        }

        // 创建管理员用户信息
        AdminUserDto adminUser = new AdminUserDto();
        adminUser.setUsername(username);
        adminUser.setPassword(password);

        // 缓存管理员信息
        String adminKey = "admin_" + username;
        CookieUtils.ADMIN_CACHER.put(adminKey, adminUser, 300L * 1000, ExpireWayEnum.AFTER_ACCESS);
        CookieUtils.setWebUserCookie(adminKey, CookieUtils.COOKIE_MAX_AGE);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "管理员登录成功");
        result.put("adminKey", adminKey);
        return result;
    }

    /**
     * 获取白名单用户列表
     *
     * @return 白名单用户列表
     */
    @RequestMapping("/getWhitelist")
    public Map<String, Object> getWhitelist() {
        List<String> whitelist = userWhitelistService.loadWhitelist();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", whitelist);
        result.put("total", whitelist.size());
        return result;
    }

    /**
     * 添加用户到白名单
     *
     * @param users 用户列表，一行一个用户
     * @return 添加结果
     */
    @RequestMapping("/addUsers")
    public Map<String, Object> addUsers(@RequestParam String users) {
        int addedCount = userWhitelistService.addUsersToWhitelist(users);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "成功添加 " + addedCount + " 个用户到白名单");
        result.put("addedCount", addedCount);
        return result;
    }

    /**
     * 从白名单中删除用户
     *
     * @param users 要删除的用户列表，用逗号分割
     * @return 删除结果
     */
    @RequestMapping("/removeUsers")
    public Map<String, Object> removeUsers(@RequestParam("users") String users) {
        if (StringUtils.isBlank(users)) {
            throw new FrontException("要删除的用户列表不能为空");
        }
        
        List<String> userList = List.of(users.split(","));
        int removedCount = userWhitelistService.removeUsersFromWhitelist(userList);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "成功从白名单中删除 " + removedCount + " 个用户");
        result.put("removedCount", removedCount);
        return result;
    }

    /**
     * 刷新白名单
     *
     * @return 刷新结果
     */
    @RequestMapping("/refreshWhitelist")
    public Map<String, Object> refreshWhitelist() {
        List<String> whitelist = userWhitelistService.loadWhitelist();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "白名单刷新成功");
        result.put("data", whitelist);
        result.put("total", whitelist.size());
        return result;
    }
} 
package cn.sleepybear.jxkpautolearn.service;

import cn.sleepybear.jxkpautolearn.dto.UserInfoDto;
import cn.sleepybear.jxkpautolearn.exception.FrontException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户白名单服务
 *
 * @author sleepybear
 * @date 2025/01/20
 */
@Slf4j
@Service
public class UserWhitelistService {

    private static final String WHITELIST_FILE = "allow-users.txt";

    /**
     * 检查用户是否在白名单中
     *
     * @param userInfoDto 用户信息
     * @return 是否在白名单中
     */
    public boolean isUserInWhitelist(UserInfoDto userInfoDto) {
        if (userInfoDto == null) {
            return false;
        }

        List<String> whitelist = loadWhitelist();
        if (whitelist.isEmpty()) {
            return true; // 如果白名单为空，允许所有用户
        }

        // 检查身份证号、姓名、手机号是否在白名单中
        String idCard = userInfoDto.getIdCard();
        String name = userInfoDto.getName();
        String tel = userInfoDto.getTel();

        return whitelist.stream().anyMatch(allowedUser -> {
            String trimmedAllowedUser = allowedUser.trim();
            return (StringUtils.isNotBlank(idCard) && idCard.equals(trimmedAllowedUser)) ||
                   (StringUtils.isNotBlank(name) && name.equals(trimmedAllowedUser)) ||
                   (StringUtils.isNotBlank(tel) && tel.equals(trimmedAllowedUser));
        });
    }

    /**
     * 加载白名单
     *
     * @return 白名单列表
     */
    public List<String> loadWhitelist() {
        try {
            Path filePath = Paths.get(WHITELIST_FILE);
            if (!Files.exists(filePath)) {
                // 如果文件不存在，创建空文件
                Files.createFile(filePath);
                return new ArrayList<>();
            }

            return Files.readAllLines(filePath, StandardCharsets.UTF_8)
                    .stream()
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("加载白名单文件失败", e);
            throw new FrontException("加载白名单文件失败: " + e.getMessage());
        }
    }

    /**
     * 添加用户到白名单
     *
     * @param users 用户列表，一行一个用户
     * @return 添加成功的用户数量
     */
    public int addUsersToWhitelist(String users) {
        if (StringUtils.isBlank(users)) {
            throw new FrontException("用户列表不能为空");
        }

        List<String> currentWhitelist = loadWhitelist();
        String[] newUsers = users.split("\n");
        int addedCount = 0;

        for (String user : newUsers) {
            String trimmedUser = user.trim();
            if (StringUtils.isNotBlank(trimmedUser) && !currentWhitelist.contains(trimmedUser)) {
                currentWhitelist.add(trimmedUser);
                addedCount++;
            }
        }

        saveWhitelist(currentWhitelist);
        return addedCount;
    }

    /**
     * 从白名单中删除用户
     *
     * @param users 要删除的用户列表
     * @return 删除成功的用户数量
     */
    public int removeUsersFromWhitelist(List<String> users) {
        if (users == null || users.isEmpty()) {
            throw new FrontException("要删除的用户列表不能为空");
        }

        List<String> currentWhitelist = loadWhitelist();
        int removedCount = 0;

        for (String user : users) {
            if (currentWhitelist.remove(user)) {
                removedCount++;
            }
        }

        saveWhitelist(currentWhitelist);
        return removedCount;
    }

    /**
     * 保存白名单到文件
     *
     * @param whitelist 白名单列表
     */
    private void saveWhitelist(List<String> whitelist) {
        try {
            Path filePath = Paths.get(WHITELIST_FILE);
            Files.write(filePath, whitelist, StandardCharsets.UTF_8);
            log.info("白名单文件已更新，共{}个用户", whitelist.size());
        } catch (IOException e) {
            log.error("保存白名单文件失败", e);
            throw new FrontException("保存白名单文件失败: " + e.getMessage());
        }
    }
} 
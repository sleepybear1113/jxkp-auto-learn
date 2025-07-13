package cn.sleepybear.jxkpautolearn.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2025/03/20 00:33
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app-config")
public class AppConfig {
    private String[] adminUsernames;

    public List<String> getAdminUsernameList() {
        if (adminUsernames == null) {
            return List.of();
        }
        return List.of(adminUsernames);
    }
}

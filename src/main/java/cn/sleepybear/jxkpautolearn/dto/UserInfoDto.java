package cn.sleepybear.jxkpautolearn.dto;

import cn.sleepybear.jxkpautolearn.utils.MyCookieJar;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2024/07/16 16:32
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 5169664476161821854L;

    private String idCard;
    private String password;
    private String cookie;
    private MyCookieJar myCookieJar = new MyCookieJar();
    private List<String> lessonKcIdList;

    private String name;
    private String tel;

    private Boolean stop = true;
    private Boolean stopping;
    private Long lastLearnTime;

    private String captchaToken;

    private Boolean admin;

    public String getPassword() {
        return null;
    }

    public String fetchPwd() {
        return password;
    }

    public String getKey() {
        return idCard;
    }
}

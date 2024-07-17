package cn.sleepybear.jxkpautolearn.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2024/07/16 16:09
 */
@Data
public class LessonInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -749593738668386745L;

    private String lessonName;
    private String kcId;
    private String url;
    private Integer learnedCount;
    private Integer totalLessonCount;
    private String status;

    public void setUrl(String url) {
        this.url = url;
        String[] split = url.split("/");
        this.kcId = split[split.length - 1];
    }

    public boolean empty() {
        return lessonName == null || url == null || learnedCount == null || totalLessonCount == null || status == null;
    }

    public boolean isFinished() {
        return !"未完成".equals(status);
    }
}

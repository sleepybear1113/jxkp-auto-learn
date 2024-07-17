package cn.sleepybear.jxkpautolearn.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2024/07/16 23:52
 */
@Data
public class CourseInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -3451324625081972101L;

    private String name;
    private String kcId;
    private Integer learnedCount;
    private Integer totalLessonCount;
    private String hours;
    private String status;

    public boolean empty() {
        return name == null || kcId == null || learnedCount == null || totalLessonCount == null || status == null;
    }
}

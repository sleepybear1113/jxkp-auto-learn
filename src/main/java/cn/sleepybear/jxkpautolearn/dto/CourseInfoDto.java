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
    
    // 新增学习状态相关字段
    private String learningStatus = "-"; // 学习状态：正在学习、等待学习、已完成、-
    private String currentLesson; // 当前学习的课时名称
    private Integer progress; // 学习进度百分比
    private Boolean checked = false; // 是否被选中学习

    public boolean empty() {
        return name == null || kcId == null || learnedCount == null || totalLessonCount == null || status == null;
    }
}

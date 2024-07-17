package cn.sleepybear.jxkpautolearn.exception;

import cn.sleepybear.jxkpautolearn.advice.ResultCodeConstant;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * @author sleepybear
 */
@Setter
@Getter
public class FrontException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -850477151980098414L;

    private Integer code;

    public FrontException() {
    }

    public FrontException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public FrontException(ResultCodeConstant.CodeEnum codeEnum, String message) {
        this(codeEnum.getCode(), message);
    }

    public FrontException(String message) {
        this(ResultCodeConstant.CodeEnum.COMMON_ERROR, message);
    }

}

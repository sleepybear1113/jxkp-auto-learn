package cn.sleepybear.jxkpautolearn.advice;

import lombok.Getter;

/**
 * @author XJX
 * @date 2021/8/10 0:38
 */
public class ResultCodeConstant {
    @Getter
    public enum CodeEnum {
        /**
         * 正常情况下的返回值
         */
        SUCCESS(0),

        /**
         * 抛出 {@link cn.sleepybear.jxkpautolearn.exception.FrontException} 时候的返回值
         */
        COMMON_ERROR(-1),

        /**
         * 请求过于频繁
         */
        REQUEST_TOO_FREQUENT(-2),

        /**
         * 无法连接至打码服务器
         */
        CANNOT_CONNECT_TO_SERVER(100),

        /**
         * 验证码识别超时
         */
        CAPTCHA_PREDICT_TIMEOUT(101),

        /**
         * 打码服务器返回的验证码识别结果为空
         */
        CAPTCHA_NO_RESPONSE(102),

        /**
         * 余额不足
         */
        BALANCE_NOT_ENOUGH(200),

        /**
         * Token 不存在
         */
        TOKEN_NOT_EXIST(201),

        /**
         * Token 已被禁用
         */
        TOKEN_DISABLED(202),

        /**
         * Token 已过期
         */
        TOKEN_EXPIRED(203),
        /**
         * Token 无效
         */
        TOKEN_INVALID(204),

        /**
         * 同一时间请求 ip 过多
         */
        IP_REQUEST_TOO_FREQUENT(301),

        /**
         * 其他错误
         */
        SYSTEM_ERROR(-9999),
        ;
        private final Integer code;

        CodeEnum(Integer code) {
            this.code = code;
        }

    }
}

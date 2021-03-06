package cn.jiangzeyin.common;

import java.lang.annotation.*;

/**
 * 预加载需要调用的方法
 * Created by jiangzeyin on 2017/10/24.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreLoadMethod {
    int value() default 0;
}

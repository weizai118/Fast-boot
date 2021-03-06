package cn.jiangzeyin.common.interceptor;

import cn.jiangzeyin.CommonPropertiesFinal;
import cn.jiangzeyin.StringUtil;
import cn.jiangzeyin.common.DefaultSystemLog;
import cn.jiangzeyin.common.ApplicationBuilder;
import cn.jiangzeyin.common.spring.SpringUtil;
import cn.jiangzeyin.util.PackageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 拦截器控制器
 *
 * @author jiangzeyin
 * Created by jiangzeyin on 2017/2/4.
 */
@Configuration
@EnableWebMvc
public class InterceptorControl extends WebMvcConfigurerAdapter {
    @Value("${" + CommonPropertiesFinal.INTERCEPTOR_INIT_PACKAGE_NAME + ":}")
    private String loadPath;
    private boolean isHash = false;

    private static final List<Class> LOAD_OK = new ArrayList<>();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        init(registry);
    }

    /**
     * @param registry 注册
     */
    private void init(InterceptorRegistry registry) {
        if (loadPath == null || loadPath.length() <= 0) {
            loadDefault(registry);
            return;
        }
        List<String> list;
        try {
            list = PackageUtil.getClassName(loadPath);
        } catch (IOException e) {
            DefaultSystemLog.ERROR().error("加载拦截器异常", e);
            loadDefault(registry);
            return;
        }
        if (list.size() <= 0) {
            loadDefault(registry);
            return;
        }
        for (String item : list) {
            Class classItem;
            try {
                classItem = Class.forName(item);
            } catch (ClassNotFoundException e) {
                DefaultSystemLog.ERROR().error("加载拦截器错误", e);
                continue;
            }
            if (classItem == null) {
                continue;
            }
            if (classItem == DefaultInterceptor.class) {
                continue;
            }
            boolean isAbstract = Modifier.isAbstract(classItem.getModifiers());
            if (isAbstract) {
                continue;
            }
            if (!BaseInterceptor.class.isAssignableFrom(classItem)) {
                continue;
            }
            loadInterceptor(classItem, registry);
        }
        loadDefault(registry);
    }

    private void loadApplicationInterceptor(InterceptorRegistry registry) {
        List<Class<? extends BaseInterceptor>> interceptorClass = ApplicationBuilder.getInstance().getInterceptorClass();
        if (interceptorClass != null) {
            for (Class<? extends BaseInterceptor> item : interceptorClass) {
                loadInterceptor(item, registry);
            }
        }
    }

    private void loadDefault(InterceptorRegistry registry) {
        if (isHash) {
            loadApplicationInterceptor(registry);
            return;
        }
        loadApplicationInterceptor(registry);
        DefaultSystemLog.LOG().info("加载默认拦截器");
        loadInterceptor(DefaultInterceptor.class, registry);
    }

    private void loadInterceptor(Class itemCls, InterceptorRegistry registry) {
        if (LOAD_OK.contains(itemCls) && !ApplicationBuilder.isRestart()) {
            DefaultSystemLog.LOG().info("重复注入拦截器" + itemCls);
            return;
        }
        InterceptorPattens interceptorPattens = (InterceptorPattens) itemCls.getAnnotation(InterceptorPattens.class);
        if (interceptorPattens == null) {
            return;
        }
        BaseInterceptor handlerInterceptor;
        try {
            handlerInterceptor = (BaseInterceptor) itemCls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            DefaultSystemLog.ERROR().error("加载拦截器错误", e);
            return;
        }
        String[] patterns = interceptorPattens.value();
        // 注册
        InterceptorRegistration registration = registry.addInterceptor(handlerInterceptor);
        registration.addPathPatterns(patterns);
        // 排除
        String[] exclude = interceptorPattens.exclude();
        if (exclude.length > 0) {
            registration.excludePathPatterns(exclude);
        }
        LOAD_OK.add(itemCls);
        DefaultSystemLog.LOG().info("加载拦截器：" + itemCls + "  " + Arrays.toString(patterns) + "  " + Arrays.toString(exclude));
        isHash = true;
    }

    /**
     * 静态资源配置
     *
     * @param registry 注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceHandler = SpringUtil.getEnvironment().getProperty(CommonPropertiesFinal.INTERCEPTOR_RESOURCE_HANDLER);
        ResourceHandlerRegistration resourceHandlerRegistration;
        if (!StringUtil.isEmpty(resourceHandler)) {
            String[] handler = StringUtil.stringToArray(resourceHandler, ",");
            resourceHandlerRegistration = registry.addResourceHandler(handler);
            // 资源文件路径
            String resourceLocation = SpringUtil.getEnvironment().getProperty(CommonPropertiesFinal.INTERCEPTOR_RESOURCE_LOCATION);
            if (resourceHandlerRegistration != null && !StringUtil.isEmpty(resourceLocation)) {
                String[] location = StringUtil.stringToArray(resourceLocation, ",");
                resourceHandlerRegistration.addResourceLocations(location);
            }
        }
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        List<HttpMessageConverter<?>> httpMessageConverters = ApplicationBuilder.getInstance().getHttpMessageConverters();
        if (httpMessageConverters != null) {
            converters.addAll(httpMessageConverters);
        }
    }

    /**
     * 默认的拦截器
     * Created by jiangzeyin on 2017/11/3.
     */
    @InterceptorPattens
    static class DefaultInterceptor extends BaseInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            super.preHandle(request, response, handler);
            reload();
            return true;
        }
    }
}

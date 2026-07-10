package com.mop.framework.config;

import com.mop.common.constant.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;

/**
 * 资源文件配置加载
 *
 * @author weiyiming
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver clr = new CookieLocaleResolver("language");
        // 默认语言
        clr.setDefaultLocale(Constants.DEFAULT_LOCALE);
        // Cookie 有效期（30天）
        clr.setCookieMaxAge(Duration.ofDays(30));
        clr.setCookiePath("/");
        // BCP 47 语言标签模式：原生解析 zh-CN、en-US 等标准格式
        // 与前端 vue-i18n、HTML lang 属性、HTTP Accept-Language 统一标准
        clr.setLanguageTagCompliant(true);
        return clr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        // 参数名（也支持 URL 参数 ?lang=zh-CN 切换语言，BCP 47 格式）
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}

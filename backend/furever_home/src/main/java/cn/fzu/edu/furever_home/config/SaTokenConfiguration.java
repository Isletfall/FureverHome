package cn.fzu.edu.furever_home.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.format.FormatterRegistry;
import org.springframework.core.convert.converter.Converter;
import cn.fzu.edu.furever_home.common.enums.AdoptionStatus;
import cn.fzu.edu.furever_home.common.enums.Gender;
import cn.fzu.edu.furever_home.common.enums.Species;

@Configuration
public class SaTokenConfiguration implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> SaRouter.match("/**")
                .notMatch("/api/auth/**")
                .notMatch("/v3/api-docs/**")
                .notMatch("/swagger-ui/**")
                .notMatch("/swagger-ui.html")
                .notMatch("/api/storage/image/**")
                .notMatch("/api/storage/video/**")
                .check(r -> StpUtil.checkLogin()))).addPathPatterns("/**");
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, AdoptionStatus>() {
            @Override
            public AdoptionStatus convert(String source) {
                if (source == null) return null;
                String s = source.trim();
                for (AdoptionStatus e : AdoptionStatus.values()) {
                    if (e.getValue().equals(s)) return e;
                    if (e.name().equalsIgnoreCase(s)) return e;
                }
                throw new IllegalArgumentException("Invalid AdoptionStatus: " + source);
            }
        });
        registry.addConverter(new Converter<String, Gender>() {
            @Override
            public Gender convert(String source) {
                if (source == null) return null;
                String s = source.trim();
                for (Gender e : Gender.values()) {
                    if (e.getValue().equals(s)) return e;
                    if (e.name().equalsIgnoreCase(s)) return e;
                }
                throw new IllegalArgumentException("Invalid Gender: " + source);
            }
        });
        registry.addConverter(new Converter<String, Species>() {
            @Override
            public Species convert(String source) {
                if (source == null) return null;
                String s = source.trim();
                for (Species e : Species.values()) {
                    if (e.getValue().equals(s)) return e;
                    if (e.name().equalsIgnoreCase(s)) return e;
                }
                throw new IllegalArgumentException("Invalid Species: " + source);
            }
        });
    }
}
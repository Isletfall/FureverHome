package cn.fzu.edu.furever_home.config;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class ApiLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(ApiLoggingAspect.class);

    @Around("execution(public * cn.fzu.edu.furever_home..controller..*(..)) && !within(cn.fzu.edu.furever_home.admin.controller..*)")
    public Object logApi(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = sra == null ? null : sra.getRequest();
        String method = request == null ? "" : request.getMethod();
        String uri = request == null ? "" : request.getRequestURI();
        String query = request == null ? "" : request.getQueryString();
        Object uid = StpUtil.isLogin() ? StpUtil.getLoginIdDefaultNull() : null;
        long start = System.currentTimeMillis();
        try {
            log.info("api {} {} {} uid={}", method, uri, query, uid);
            Object result = pjp.proceed();
            long cost = System.currentTimeMillis() - start;
            log.info("api done {} {} cost={}ms uid={}", method, uri, cost, uid);
            return result;
        } catch (Throwable t) {
            long cost = System.currentTimeMillis() - start;
            log.error("api error {} {} cost={}ms uid={} msg={}", method, uri, cost, uid, t.getMessage());
            throw t;
        }
    }
}
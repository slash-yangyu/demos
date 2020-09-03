package com.keesail.klh.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Objects;

/**
 * @ClassName : WebLogCollect
 * @Description :统一log处理
 * @Author : YangYu
 * @Date: 2020/6/18
 * 		<dependency>
 * 			<groupId>org.springframework.boot</groupId>
 * 			<artifactId>spring-boot-starter-aop</artifactId>
 * 		</dependency>
 */

@Aspect
@Component
@Slf4j
public class WebLogCollect {

    ThreadLocal<Long> startTime = new ThreadLocal<>();
    /**
     * 切面controller
     */
    @Pointcut("execution(* com.keesail.klh.controller.*.*(..))")
    public void pointCut(){
    }

    @Before("pointCut()")
    public void doBefore(JoinPoint joinPoint){
        startTime.set(System.currentTimeMillis());
        log.info("START TIME : "+ startTime.get());
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        String addr = request.getRemoteAddr();

        log.info("URL:{}",request.getRequestURI());
        log.info("METHOD:{}",request.getMethod());
        log.info("ADDR:{}",addr);
        log.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        log.info("REQUEST ARGS : " + JSON.toJSONString(joinPoint.getArgs()));

        Enumeration<String> attributeNames = request.getParameterNames();
        while(attributeNames.hasMoreElements()){
            String key = attributeNames.nextElement();
            String value = request.getParameter(key);
            log.info("KEY:{},VALUE:{}",addr,value);
        }
    }

    @After("pointCut()")
    public void doAfter(JoinPoint joinPoint) {
        log.info("RESPONSE : " + joinPoint);
        log.info("SPEND TIME : " + (System.currentTimeMillis() - startTime.get()));
    }
}


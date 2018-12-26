package com.myself.lock.example.aop;

import com.myself.lock.lock.DistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;

/**
 * 类名称：LockAspect<br>
 * 类描述：<br>
 * 创建时间：2018年12月26日<br>
 *
 * @author maopanpan
 * @version 1.0.0
 */
@Aspect
@Component
public class LockAspect {

    @Autowired
    private DistributedLock distributedLock;

    @Around("@annotation(lock)")
    public void handler(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
        String key = lock.value();
        if ("".equals(key)) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Annotation[][] annotations = method.getParameterAnnotations();
            Parameter[] parameters = method.getParameters();
            Annotation[] annotation =method.getDeclaredAnnotations();
            Object[] agrs = joinPoint.getArgs();
            int index = 0;
            for (Annotation[] anns : annotations) {
                if (Objects.nonNull(annotations) && anns.length > 0) {
                    for (Annotation annotation1 : anns) {
                        if (annotation1 instanceof Param) {
                            key = (String) agrs[index];
                            break;
                        }
                    }
                }
                index++;
            }
        }
        if (distributedLock.tryLock(key)) {
            //锁定
            try {
                joinPoint.proceed();
            } finally {
                //解锁
                distributedLock.unLock(key);
            }
        } else {
            throw new Exception("系统资源繁忙，稍后再试");
        }


    }
}

package com.myself.lock.example.aop;

import com.myself.lock.lock.DistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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

    ExpressionParser parser = new SpelExpressionParser();
    LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    @Around("@annotation(lock)")
    public void handler(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
//        ExpressionParser parser = new SpelExpressionParser();
//        EvaluationContext context=new StandardEvaluationContext(joinPoint);
//        String name= (String) parser.parseExpression("name").getValue(context);
        Object[] args = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String spel = lock.value();
        String[] params = discoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();
        for (int len = 0; len < params.length; len++) {
            context.setVariable(params[len], args[len]);
        }
        Expression expression = parser.parseExpression(spel);
        System.out.println(expression.getValue(context, String.class));
            joinPoint.proceed();
        //        String key = lock.value();
//        if ("".equals(key)) {
//            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//            Method method = signature.getMethod();
//            Annotation[][] annotations = method.getParameterAnnotations();
//            Parameter[] parameters = method.getParameters();
//            Annotation[] annotation =method.getDeclaredAnnotations();
//            Object[] agrs = joinPoint.getArgs();
//            int index = 0;
//            for (Annotation[] anns : annotations) {
//                if (Objects.nonNull(annotations) && anns.length > 0) {
//                    for (Annotation annotation1 : anns) {
//                        if (annotation1 instanceof Param) {
//                            key = (String) agrs[index];
//                            break;
//                        }
//                    }
//                }
//                index++;
//            }
//        }
//        if (distributedLock.tryLock(key)) {
//            //锁定
//            try {
//                joinPoint.proceed();
//            } finally {
//                //解锁
//                distributedLock.unLock(key);
//            }
//        } else {
//            throw new Exception("系统资源繁忙，稍后再试");
//        }


    }
}

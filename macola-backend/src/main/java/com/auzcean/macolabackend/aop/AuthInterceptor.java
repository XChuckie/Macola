package com.auzcean.macolabackend.aop;

import com.auzcean.macolabackend.annotation.AuthCheck;
import com.auzcean.macolabackend.exception.BusinessException;
import com.auzcean.macolabackend.exception.ErrorCode;
import com.auzcean.macolabackend.model.entity.User;
import com.auzcean.macolabackend.model.enums.UserRoleEnum;
import com.auzcean.macolabackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 连接点，起到连接业务中插入注解的位置
     * @param authCheck 权限校验注解
     * @return
     */
    @Around(value = "@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获得当前登录用户
        User currentUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 如果不需要全乡，则放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();  // 对于没有指定角色的接口可直接访问，即：无权限登录接口
        }

        // 接下来：必须有权限才能够通过
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(currentUser.getUserRole());
        // 没有权限，则拒绝 -> 必须制定用户或者管理员
        if (userRoleEnum == null) throw new BusinessException(ErrorCode.NO_AUTH_ERROR);

        // 要求必须是管理员才能够访问，而用户没有管理员权限则会被拒绝
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

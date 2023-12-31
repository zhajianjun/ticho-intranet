package top.ticho.intranet.server.domain.service.login;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.ticho.boot.security.constant.BaseSecurityConst;
import top.ticho.boot.security.handle.load.LoadUserService;
import top.ticho.boot.view.enums.HttpErrCode;
import top.ticho.boot.view.util.Assert;
import top.ticho.intranet.server.domain.repository.RoleRepository;
import top.ticho.intranet.server.domain.repository.UserRepository;
import top.ticho.intranet.server.domain.repository.UserRoleRepository;
import top.ticho.intranet.server.infrastructure.core.enums.UserStatus;
import top.ticho.intranet.server.infrastructure.entity.Role;
import top.ticho.intranet.server.infrastructure.entity.User;
import top.ticho.intranet.server.infrastructure.entity.UserRole;
import top.ticho.intranet.server.interfaces.dto.SecurityUser;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zhajianjun
 * @date 2024-01-08 20:30
 */
@Component(BaseSecurityConst.LOAD_USER_TYPE_USERNAME)
@Primary
@Slf4j
public class DefaultUsernameLoadUserService implements LoadUserService {

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public SecurityUser load(String account) {
        // @formatter:off
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        // 用户信息校验
        User user = userRepository.getByUsername(account);
        Assert.isNotNull(user, HttpErrCode.NOT_LOGIN, "用户或者密码不正确");
        Integer status = user.getStatus();
        String message = UserStatus.getByCode(status);
        boolean normal = Objects.equals(status, UserStatus.NORMAL.code());
        Assert.isTrue(normal, HttpErrCode.NOT_LOGIN, String.format("用户%s", message));
        return getSecurityUser(user);
        // @formatter:on
    }

    private SecurityUser getSecurityUser(User user) {
        List<UserRole> userRoles = userRoleRepository.listByUserId(user.getId());
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<Role> roles = roleRepository.listByIds(roleIds);
        List<String> codes = roles.stream().filter(x -> Objects.equals(1, x.getStatus())).map(Role::getCode).collect(Collectors.toList());
        SecurityUser securityUser = new SecurityUser();
        securityUser.setUsername(user.getUsername());
        securityUser.setPassword(user.getPassword());
        securityUser.setRoles(codes);
        securityUser.setStatus(user.getStatus());
        return securityUser;
    }

}

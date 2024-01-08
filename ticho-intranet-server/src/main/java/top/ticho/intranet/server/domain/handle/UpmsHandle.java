package top.ticho.intranet.server.domain.handle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import top.ticho.boot.security.util.BaseUserUtil;
import top.ticho.boot.web.util.TreeUtil;
import top.ticho.intranet.server.domain.repository.MenuRepository;
import top.ticho.intranet.server.domain.repository.RoleMenuRepository;
import top.ticho.intranet.server.domain.repository.RoleRepository;
import top.ticho.intranet.server.domain.repository.UserRepository;
import top.ticho.intranet.server.domain.repository.UserRoleRepository;
import top.ticho.intranet.server.infrastructure.core.enums.MenuType;
import top.ticho.intranet.server.infrastructure.entity.Menu;
import top.ticho.intranet.server.infrastructure.entity.Role;
import top.ticho.intranet.server.infrastructure.entity.RoleMenu;
import top.ticho.intranet.server.infrastructure.entity.User;
import top.ticho.intranet.server.infrastructure.entity.UserRole;
import top.ticho.intranet.server.interfaces.assembler.MenuAssembler;
import top.ticho.intranet.server.interfaces.assembler.RoleAssembler;
import top.ticho.intranet.server.interfaces.assembler.UserAssembler;
import top.ticho.intranet.server.interfaces.dto.MenuDtlDTO;
import top.ticho.intranet.server.interfaces.dto.RoleDTO;
import top.ticho.intranet.server.interfaces.dto.RoleMenuDtlDTO;
import top.ticho.intranet.server.interfaces.dto.SecurityUser;
import top.ticho.intranet.server.interfaces.dto.UserRoleMenuDtlDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 *
 * @author zhajianjun
* @date 2023-12-17 08:30
 */
public class UpmsHandle {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    @Autowired
    private MenuRepository menuRepository;

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return {@link UserRoleMenuDtlDTO}
     */
    public UserRoleMenuDtlDTO getUserDtl(String username) {
        if (StrUtil.isBlank(username)) {
            SecurityUser baseSecurityUser = BaseUserUtil.getCurrentUser();
            SecurityUser currentUser = Optional.ofNullable(baseSecurityUser).orElseGet(SecurityUser::new);
            username = currentUser.getUsername();
        }
        User user = userRepository.getByUsername(username);
        UserRoleMenuDtlDTO userRoleMenuDtlDTO = UserAssembler.INSTANCE.entityToDtl(user);
        if (userRoleMenuDtlDTO == null) {
            return null;
        }
        List<UserRole> userRoles = userRoleRepository.listByUserId(user.getId());
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        RoleMenuDtlDTO roleMenuFuncDtl = mergeRoleByIds(roleIds, false, false);
        if (roleMenuFuncDtl == null) {
            return null;
        }
        userRoleMenuDtlDTO.setRoleIds(roleMenuFuncDtl.getRoleIds());
        userRoleMenuDtlDTO.setRoleCodes(roleMenuFuncDtl.getRoleCodes());
        userRoleMenuDtlDTO.setMenuIds(roleMenuFuncDtl.getMenuIds());
        userRoleMenuDtlDTO.setRoles(roleMenuFuncDtl.getRoles());
        userRoleMenuDtlDTO.setMenus(roleMenuFuncDtl.getMenus());
        return userRoleMenuDtlDTO;
    }


    /**
     * 合并菜单按角色code列表
     *
     * @param roleCodes 角色code列表
     * @param showAll 显示所有信息，匹配到的信息，设置匹配字段checkbox=true
     * @param treeHandle 是否进行树化
     * @return {@link RoleMenuDtlDTO}
     */
    public RoleMenuDtlDTO mergeRoleByCodes(List<String> roleCodes, boolean showAll, boolean treeHandle) {
        if (CollUtil.isEmpty(roleCodes)) {
            return null;
        }
        // 根据角色id列表 查询角色信息
        List<Role> roles = roleRepository.listByCodes(roleCodes);
        return getRoleMenuDtl(roles, showAll, treeHandle);
    }

    /**
     * 获取状态正常的菜单流
     *
     * @param roleCodes 角色编码
     * @param map 转换
     * @return {@link Stream}<{@link T}>
     */
    public <T> Stream<T> getMenusByRoleCodes(List<String> roleCodes, Function<Menu, T> map) {
        if (CollUtil.isEmpty(roleCodes)) {
            return Stream.empty();
        }
        // 根据角色id列表 查询角色信息
        List<Role> roles = roleRepository.listByCodes(roleCodes);
        List<Long> roleIds = roles
            .stream()
            .filter(x-> Objects.equals(1, x.getStatus()))
            .map(Role::getId)
            .collect(Collectors.toList());
        if (CollUtil.isEmpty(roleIds)) {
            return Stream.empty();
        }
        // 根据角色id列表 查询角色菜单关联信息
        List<RoleMenu> roleMenus = roleMenuRepository.listByRoleIds(roleIds);
        if (CollUtil.isEmpty(roleMenus)) {
            return Stream.empty();
        }
        // 合并的角色后所有的菜单
        List<Long> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
        if (CollUtil.isEmpty(menuIds)) {
            return Stream.empty();
        }
        // 菜单信息
        List<Menu> menus = menuRepository.list();
        return menus
            .stream()
            .filter(x-> menuIds.contains(x.getId()))
            .filter(x-> Objects.equals(1, x.getStatus()))
            .map(map)
            .filter(Objects::nonNull);
    }

    public List<String> getPerms(List<String> roleCodes) {
        // @formatter:off
        if (CollUtil.isEmpty(roleCodes)) {
            return Collections.emptyList();
        }
        Function<Menu, String> identity = menu -> {
            if (!Objects.equals(menu.getStatus(), 1) || !Objects.equals(menu.getType(), MenuType.BUTTON.code())) {
                return null;
            }
            return menu.getPerms();
        };
        return getMenusByRoleCodes(roleCodes, identity)
            .map(x-> x.split(","))
            .flatMap(Arrays::stream)
            .collect(Collectors.toList());
        // @formatter:on
    }

    /**
     * 合并菜单按角色id
     *
     * @param roleIds 角色id列表
     * @param showAll 显示所有信息，匹配到的信息，设置匹配字段checkbox=true
     * @param treeHandle 是否进行树化
     * @return {@link RoleMenuDtlDTO}
     */
    public RoleMenuDtlDTO mergeRoleByIds(List<Long> roleIds, boolean showAll, boolean treeHandle) {
        if (CollUtil.isEmpty(roleIds)) {
            return null;
        }
        // 1.根据角色id列表查询角色信息、菜单信息、角色菜单信息、角色权限标识信息、菜单权限标识信息
        // 根据角色id列表 查询角色信息
        List<Role> roles = roleRepository.listByIds(roleIds);
        return getRoleMenuDtl(roles, showAll, treeHandle);
    }

    private RoleMenuDtlDTO getRoleMenuDtl(List<Role> roles, boolean showAll, boolean treeHandle) {
        // @formatter:off
        RoleMenuDtlDTO roleMenuDtlDTO = new RoleMenuDtlDTO();
        List<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
         roleMenuDtlDTO.setRoleIds(roleIds);
        if (CollUtil.isEmpty(roleIds)) {
            return null;
        }
        // 根据角色id列表 查询角色菜单关联信息
        List<RoleMenu> roleMenus = roleMenuRepository.listByRoleIds(roleIds);
        // 合并的角色后所有的菜单
        List<Long> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
        // 菜单信息
        List<Menu> menus = menuRepository.list();
        // 查询到的角色信息组装填充
        List<RoleDTO> roleDtos = new ArrayList<>();
        roleIds = new ArrayList<>();
        List<String> roleCodes = new ArrayList<>();
        for (Role role : roles) {
            RoleDTO roleDTO = RoleAssembler.INSTANCE.entityToDto(role);
            roleIds.add(role.getId());
            roleCodes.add(role.getCode());
            roleDtos.add(roleDTO);
        }
        roleMenuDtlDTO.setRoleCodes(roleCodes);
        roleMenuDtlDTO.setRoles(roleDtos);
        List<String> perms = new ArrayList<>();
        // 菜单信息过滤规则
        Predicate<MenuDtlDTO> menuFilter = x -> menuIds.contains(x.getId());
        // 菜单信息操作
        Consumer<MenuDtlDTO> menuPeek = x-> {
            x.setCheckbox(true);
            perms.addAll(x.getPerms());
        };
        // 如果展示全部字段，匹配的数据进行填充checkbox=true
        if (showAll) {
            menuFilter = null;
            menuPeek = x -> {
                perms.addAll(x.getPerms());
                x.setCheckbox(menuIds.contains(x.getId()));
            };
        }
        // 根据菜单信息，填充权限标识信息
        List<MenuDtlDTO> menuFuncDtls = getMenuDtls(menus, menuFilter, menuPeek);
        roleMenuDtlDTO.setMenus(menuFuncDtls);
        if (!treeHandle) {
            return roleMenuDtlDTO;
        }
        // 菜单信息规整为树结构
        List<MenuDtlDTO> tree = TreeUtil.tree(menuFuncDtls, 0L);
        roleMenuDtlDTO.setMenus(tree);
        roleMenuDtlDTO.setMenuIds(menuIds);
        roleMenuDtlDTO.setPerms(perms);
        return roleMenuDtlDTO;
        // @formatter:on
    }

    // @formatter:off

    /**
     * 菜单信息转换、过滤、执行规则信息
     *
     * @param menus 菜单
     * @param filter 过滤规则
     * @param peek 执行规则
     * @return {@link List}<{@link MenuDtlDTO}>
     */
    public List<MenuDtlDTO> getMenuDtls(List<Menu> menus, Predicate<MenuDtlDTO> filter, Consumer<MenuDtlDTO> peek) {
        if (filter == null) {
            filter = x -> true;
        }
        if (peek == null) {
            peek = x -> {};
        }
        return menus
            .stream()
            .map(MenuAssembler.INSTANCE::entityToDtlDto)
            .filter(filter)
            .peek(peek)
            .sorted(Comparator.comparing(MenuDtlDTO::getParentId).thenComparing(Comparator.nullsLast(Comparator.comparing(MenuDtlDTO::getSort))))
            .collect(Collectors.toList());
    }

    // @formatter:on

}

package top.ticho.intranet.server.interfaces.facade;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.ticho.boot.view.core.PageResult;
import top.ticho.boot.view.core.Result;
import top.ticho.intranet.server.application.service.UserService;
import top.ticho.intranet.server.interfaces.dto.UserDTO;
import top.ticho.intranet.server.interfaces.dto.UserPasswordDTO;
import top.ticho.intranet.server.interfaces.dto.UserRoleDTO;
import top.ticho.intranet.server.interfaces.dto.UserRoleMenuDtlDTO;
import top.ticho.intranet.server.interfaces.query.UserQuery;

/**
 * 用户信息 控制器
 *
 * @author zhajianjun
 * @date 2024-01-08 20:30
 */
@RestController
@RequestMapping("user")
@Api(tags = "用户信息")
@ApiSort(30)
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("@perm.hasPerms('upms:user:save')")
    @ApiOperation(value = "保存用户信息")
    @ApiOperationSupport(order = 10)
    @PostMapping
    public Result<Void> save(@RequestBody UserDTO userDTO) {
        userService.save(userDTO);
        return Result.ok();
    }

    @PreAuthorize("@perm.hasPerms('upms:user:remove')")
    @ApiOperation(value = "删除用户信息")
    @ApiOperationSupport(order = 20)
    @ApiImplicitParam(value = "编号", name = "id", required = true)
    @DeleteMapping
    public Result<Void> removeById(@RequestParam("id") Long id) {
        userService.removeById(id);
        return Result.ok();
    }

    @PreAuthorize("@perm.hasPerms('upms:user:update')")
    @ApiOperation(value = "修改用户信息", notes = "无法修改密码")
    @ApiOperationSupport(order = 30)
    @PutMapping
    public Result<Void> update(@RequestBody UserDTO userDTO) {
        userService.updateById(userDTO);
        return Result.ok();
    }

    @PreAuthorize("@perm.hasPerms('upms:user:updatePassword')")
    @ApiOperation(value = "修改用户密码", notes = "修改用户密码")
    @ApiOperationSupport(order = 50)
    @PutMapping("updatePassword")
    public Result<Void> updatePassword(@RequestBody UserPasswordDTO userDetailDto) {
        userService.updatePassword(userDetailDto);
        return Result.ok();
    }

    @PreAuthorize("@perm.hasPerms('upms:user:getById')")
    @ApiOperation(value = "主键查询用户信息")
    @ApiOperationSupport(order = 50)
    @ApiImplicitParam(value = "编号", name = "id", required = true)
    @GetMapping
    public Result<UserDTO> getById(@RequestParam("id") Long id) {
        return Result.ok(userService.getById(id));
    }

    @PreAuthorize("@perm.hasPerms('upms:user:page')")
    @ApiOperation(value = "分页查询用户信息")
    @ApiOperationSupport(order = 60)
    @GetMapping("page")
    public Result<PageResult<UserDTO>> page(UserQuery query) {
        return Result.ok(userService.page(query));
    }

    @PreAuthorize("@perm.hasPerms('upms:user:getUserDtl')")
    @ApiOperation(value = "查询用户角色菜单权限标识信息")
    @ApiOperationSupport(order = 70)
    @GetMapping("getUserDtl")
    public Result<UserRoleMenuDtlDTO> getUserDtl(String username) {
        return Result.ok(userService.getUserDtl(username));
    }

    @PreAuthorize("@perm.hasPerms('upms:user:bindRole')")
    @ApiOperation(value = "用户绑定角色信息")
    @ApiOperationSupport(order = 80)
    @PostMapping("bindRole")
    public Result<Void> bindRole(@RequestBody UserRoleDTO userRoleDTO) {
        userService.bindRole(userRoleDTO);
        return Result.ok();
    }

}

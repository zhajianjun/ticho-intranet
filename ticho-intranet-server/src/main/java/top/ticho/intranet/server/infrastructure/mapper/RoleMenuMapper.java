package top.ticho.intranet.server.infrastructure.mapper;

import org.springframework.stereotype.Repository;
import top.ticho.boot.datasource.mapper.RootMapper;
import top.ticho.intranet.server.infrastructure.entity.RoleMenu;

/**
 * 角色菜单关联关系 mapper
 *
 * @author zhajianjun
 * @date 2024-01-08 20:30
 */
@Repository
public interface RoleMenuMapper extends RootMapper<RoleMenu> {

}
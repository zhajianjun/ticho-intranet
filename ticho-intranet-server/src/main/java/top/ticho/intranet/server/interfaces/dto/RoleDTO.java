package top.ticho.intranet.server.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色信息DTO
 *
 * @author zhajianjun
 * @date 2024-01-08 20:30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "角色信息DTO")
public class RoleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键编号; */
    @ApiModelProperty(value = "主键编号", position = 10)
    private Long id;

    /** 角色编码 */
    @ApiModelProperty(value = "角色编码", position = 20)
    private String code;

    /** 角色名称 */
    @ApiModelProperty(value = "角色名称", position = 30)
    private String name;

    /** 状态;1-正常,0-禁用 */
    @ApiModelProperty(value = "状态;1-正常,0-禁用", position = 35)
    private Integer status;

    /** 备注信息 */
    @ApiModelProperty(value = "备注信息", position = 50)
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", position = 60)
    private LocalDateTime createTime;

    /** 菜单id列表 */
    @ApiModelProperty(value = "菜单id列表", position = 20)
    private List<Long> menuIds;

}

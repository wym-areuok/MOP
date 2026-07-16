package com.mop.common.core.domain.entity;

import com.mop.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单权限表 sys_menu
 * <p>
 * 字段描述与 mop_initial.sql 中 sys_menu 表 MS_Description 保持一致。
 *
 * @author weiyiming
 */
@Schema(description = "系统菜单")
public class SysMenu extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID", example = "1")
    private Long menuId;

    @Schema(description = "菜单名称", required = true, example = "系统管理")
    private String menuName;

    @Schema(description = "父菜单名称", accessMode = Schema.AccessMode.READ_ONLY)
    private String parentName;

    @Schema(description = "父菜单ID", example = "0")
    private Long parentId;

    @Schema(description = "显示顺序", example = "1")
    private Integer orderNum;

    @Schema(description = "路由地址", example = "system")
    private String path;

    @Schema(description = "组件路径", example = "system/user/index")
    private String component;

    @Schema(description = "路由参数", example = "{\"id\":1}")
    private String query;

    @Schema(description = "路由名称", example = "SystemUser")
    private String routeName;

    @Schema(description = "是否为外链（0=是 1=否）", allowableValues = {"0", "1"}, example = "1")
    private String isFrame;

    @Schema(description = "是否缓存（0=缓存 1=不缓存）", allowableValues = {"0", "1"}, example = "0")
    private String isCache;

    @Schema(description = "菜单类型（M=目录 C=菜单 F=按钮）", allowableValues = {"M", "C", "F"}, example = "C")
    private String menuType;

    @Schema(description = "显示状态（0=显示 1=隐藏）", allowableValues = {"0", "1"}, example = "0")
    private String visible;

    @Schema(description = "菜单状态（0=正常 1=停用）", allowableValues = {"0", "1"}, example = "0")
    private String status;

    @Schema(description = "权限标识", example = "system:user:list")
    private String perms;

    @Schema(description = "菜单图标", example = "user")
    private String icon;

    @Schema(description = "子菜单", accessMode = Schema.AccessMode.READ_ONLY)
    private List<SysMenu> children = new ArrayList<SysMenu>();

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    @NotBlank(message = "{menu.name.not.blank}")
    @Size(min = 0, max = 50, message = "{menu.name.size}")
    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @NotNull(message = "{menu.sort.not.null}")
    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    @Size(min = 0, max = 200, message = "{menu.path.size}")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Size(min = 0, max = 255, message = "{menu.component.size}")
    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getIsFrame() {
        return isFrame;
    }

    public void setIsFrame(String isFrame) {
        this.isFrame = isFrame;
    }

    public String getIsCache() {
        return isCache;
    }

    public void setIsCache(String isCache) {
        this.isCache = isCache;
    }

    @NotBlank(message = "{menu.type.not.blank}")
    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public String getVisible() {
        return visible;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Size(min = 0, max = 100, message = "{menu.perms.size}")
    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<SysMenu> getChildren() {
        return children;
    }

    public void setChildren(List<SysMenu> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("menuId", getMenuId())
                .append("menuName", getMenuName())
                .append("parentId", getParentId())
                .append("orderNum", getOrderNum())
                .append("path", getPath())
                .append("component", getComponent())
                .append("query", getQuery())
                .append("routeName", getRouteName())
                .append("isFrame", getIsFrame())
                .append("IsCache", getIsCache())
                .append("menuType", getMenuType())
                .append("visible", getVisible())
                .append("status ", getStatus())
                .append("perms", getPerms())
                .append("icon", getIcon())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

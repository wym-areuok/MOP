package com.mop.common.core.domain.entity;

import com.mop.common.annotation.Excel;
import com.mop.common.annotation.Excel.ColumnType;
import com.mop.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Set;

/**
 * 角色表 sys_role
 * <p>
 * 字段描述与 mop_initial.sql 中 sys_role 表 MS_Description 保持一致。
 *
 * @author weiyiming
 */
@Schema(description = "系统角色")
public class SysRole extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID", example = "1")
    @Excel(name = "角色序号", cellType = ColumnType.NUMERIC)
    private Long roleId;

    @Schema(description = "角色名称", required = true, example = "超级管理员")
    @Excel(name = "角色名称")
    private String roleName;

    @Schema(description = "角色权限字符串", required = true, example = "admin")
    @Excel(name = "角色权限")
    private String roleKey;

    @Schema(description = "显示顺序", example = "1")
    @Excel(name = "角色排序")
    private Integer roleSort;

    @Schema(description = "数据范围（1=全部数据权限 2=自定义数据权限 3=本部门数据权限 4=本部门及以下数据权限 5=仅本人数据权限）",
            allowableValues = {"1", "2", "3", "4", "5"}, example = "1")
    @Excel(name = "数据范围", readConverterExp = "1=所有数据权限,2=自定义数据权限,3=本部门数据权限,4=本部门及以下数据权限,5=仅本人数据权限")
    private String dataScope;

    @Schema(description = "菜单树选择项是否关联显示")
    private boolean menuCheckStrictly;

    @Schema(description = "部门树选择项是否关联显示")
    private boolean deptCheckStrictly;

    @Schema(description = "角色状态（0=正常 1=停用）", allowableValues = {"0", "1"}, example = "0")
    @Excel(name = "角色状态", readConverterExp = "0=正常,1=停用")
    private String status;

    @Schema(description = "删除标志（0=存在 2=删除）", allowableValues = {"0", "2"}, example = "0", accessMode = Schema.AccessMode.READ_ONLY)
    private String delFlag;

    @Schema(description = "用户是否已分配此角色", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean flag = false;

    @Schema(description = "菜单ID组", accessMode = Schema.AccessMode.WRITE_ONLY)
    private Long[] menuIds;

    @Schema(description = "部门ID组（数据权限）", accessMode = Schema.AccessMode.WRITE_ONLY)
    private Long[] deptIds;

    @Schema(description = "角色菜单权限集合", accessMode = Schema.AccessMode.READ_ONLY)
    private Set<String> permissions;

    public SysRole() {

    }

    public SysRole(Long roleId) {
        this.roleId = roleId;
    }

    public static boolean isAdmin(Long roleId) {
        return roleId != null && 1L == roleId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public boolean isAdmin() {
        return isAdmin(this.roleId);
    }

    @NotBlank(message = "{role.name.not.blank}")
    @Size(min = 0, max = 30, message = "{role.name.size}")
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @NotBlank(message = "{role.key.not.blank}")
    @Size(min = 0, max = 100, message = "{role.key.size}")
    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    @NotNull(message = "{role.sort.not.null}")
    public Integer getRoleSort() {
        return roleSort;
    }

    public void setRoleSort(Integer roleSort) {
        this.roleSort = roleSort;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public boolean isMenuCheckStrictly() {
        return menuCheckStrictly;
    }

    public void setMenuCheckStrictly(boolean menuCheckStrictly) {
        this.menuCheckStrictly = menuCheckStrictly;
    }

    public boolean isDeptCheckStrictly() {
        return deptCheckStrictly;
    }

    public void setDeptCheckStrictly(boolean deptCheckStrictly) {
        this.deptCheckStrictly = deptCheckStrictly;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public Long[] getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(Long[] menuIds) {
        this.menuIds = menuIds;
    }

    public Long[] getDeptIds() {
        return deptIds;
    }

    public void setDeptIds(Long[] deptIds) {
        this.deptIds = deptIds;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("roleId", getRoleId())
                .append("roleName", getRoleName())
                .append("roleKey", getRoleKey())
                .append("roleSort", getRoleSort())
                .append("dataScope", getDataScope())
                .append("menuCheckStrictly", isMenuCheckStrictly())
                .append("deptCheckStrictly", isDeptCheckStrictly())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

package com.mop.common.core.domain.entity;

import com.mop.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门表 sys_dept
 * <p>
 * 字段描述与 mop_initial.sql 中 sys_dept 表 MS_Description 保持一致。
 *
 * @author weiyiming
 */
@Schema(description = "系统部门")
public class SysDept extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Schema(description = "部门ID", example = "100")
    private Long deptId;

    @Schema(description = "父部门ID")
    private Long parentId;

    @Schema(description = "祖级列表", accessMode = Schema.AccessMode.READ_ONLY)
    private String ancestors;

    @Schema(description = "部门名称", required = true, example = "研发部")
    private String deptName;

    @Schema(description = "显示顺序", example = "1")
    private Integer orderNum;

    @Schema(description = "负责人", example = "张三")
    private String leader;

    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "dept@example.com")
    private String email;

    @Schema(description = "部门状态（0=正常 1=停用）", allowableValues = {"0", "1"}, example = "0")
    private String status;

    @Schema(description = "删除标志（0=存在 2=删除）", allowableValues = {"0", "2"}, example = "0", accessMode = Schema.AccessMode.READ_ONLY)
    private String delFlag;

    @Schema(description = "父部门名称", accessMode = Schema.AccessMode.READ_ONLY)
    private String parentName;

    @Schema(description = "子部门", accessMode = Schema.AccessMode.READ_ONLY)
    private List<SysDept> children = new ArrayList<SysDept>();

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getAncestors() {
        return ancestors;
    }

    public void setAncestors(String ancestors) {
        this.ancestors = ancestors;
    }

    @NotBlank(message = "{dept.name.not.blank}")
    @Size(min = 0, max = 30, message = "{dept.name.size}")
    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    @NotNull(message = "{dept.sort.not.null}")
    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    @Size(min = 0, max = 11, message = "{dept.phone.size}")
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Email(message = "{dept.email.invalid}")
    @Size(min = 0, max = 50, message = "{dept.email.size}")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public List<SysDept> getChildren() {
        return children;
    }

    public void setChildren(List<SysDept> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("deptId", getDeptId())
                .append("parentId", getParentId())
                .append("ancestors", getAncestors())
                .append("deptName", getDeptName())
                .append("orderNum", getOrderNum())
                .append("leader", getLeader())
                .append("phone", getPhone())
                .append("email", getEmail())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}

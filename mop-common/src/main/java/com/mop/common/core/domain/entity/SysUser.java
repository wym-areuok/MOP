package com.mop.common.core.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mop.common.annotation.Excel;
import com.mop.common.annotation.Excel.ColumnType;
import com.mop.common.annotation.Excel.Type;
import com.mop.common.annotation.Excels;
import com.mop.common.core.domain.BaseEntity;
import com.mop.common.utils.SecurityUtils;
import com.mop.common.xss.Xss;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * 用户对象 sys_user
 * <p>
 * 字段描述与 mop_initial.sql 中 sys_user 表 MS_Description 保持一致。
 *
 * @author weiyiming
 */
@Schema(description = "系统用户")
public class SysUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1")
    @Excel(name = "用户序号", type = Type.EXPORT, cellType = ColumnType.NUMERIC, prompt = "用户编号")
    private Long userId;

    @Schema(description = "部门ID", example = "103")
    @Excel(name = "部门编号", type = Type.IMPORT)
    private Long deptId;

    @Schema(description = "用户账号", required = true, example = "admin")
    @Excel(name = "登录名称")
    private String userName;

    @Schema(description = "用户昵称", example = "管理员")
    @Excel(name = "用户名称")
    private String nickName;

    /**
     * 用户类型（00系统用户）— SysUser 的 userType 字段通过 DeferredImportSelector 注入，此处不声明
     */
    @Schema(description = "用户邮箱", example = "admin@example.com")
    @Excel(name = "用户邮箱")
    private String email;

    @Schema(description = "手机号码", example = "13800138000")
    @Excel(name = "手机号码", cellType = ColumnType.TEXT)
    private String phonenumber;

    @Schema(description = "用户性别（0=男 1=女 2=未知）", allowableValues = {"0", "1", "2"}, example = "0")
    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "密码", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Schema(description = "账号状态（0=正常 1=停用）", allowableValues = {"0", "1"}, example = "0")
    @Excel(name = "账号状态", readConverterExp = "0=正常,1=停用")
    private String status;

    @Schema(description = "删除标志（0=存在 2=删除）", allowableValues = {"0", "2"}, example = "0", accessMode = Schema.AccessMode.READ_ONLY)
    private String delFlag;

    @Schema(description = "最后登录IP", accessMode = Schema.AccessMode.READ_ONLY)
    @Excel(name = "最后登录IP", type = Type.EXPORT)
    private String loginIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后登录时间", accessMode = Schema.AccessMode.READ_ONLY)
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date loginDate;

    @Schema(description = "密码最后更新时间", accessMode = Schema.AccessMode.READ_ONLY)
    private Date pwdUpdateDate;

    @Schema(description = "部门对象")
    @Excels({
            @Excel(name = "部门名称", targetAttr = "deptName", type = Type.EXPORT),
            @Excel(name = "部门负责人", targetAttr = "leader", type = Type.EXPORT)
    })
    private SysDept dept;

    @Schema(description = "角色列表")
    private List<SysRole> roles;

    @Schema(description = "角色ID组", accessMode = Schema.AccessMode.WRITE_ONLY)
    private Long[] roleIds;

    @Schema(description = "岗位ID组", accessMode = Schema.AccessMode.WRITE_ONLY)
    private Long[] postIds;

    @Schema(description = "角色ID")
    private Long roleId;

    public SysUser() {

    }

    public SysUser(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return SecurityUtils.isAdmin(this.userId);
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    @Xss(message = "{user.nickname.xss}")
    @Size(min = 0, max = 30, message = "{user.nickname.size}")
    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Xss(message = "{user.username.xss}")
    @NotBlank(message = "{user.username.not.blank}")
    @Size(min = 0, max = 30, message = "{user.username.size}")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Email(message = "{user.email.invalid}")
    @Size(min = 0, max = 50, message = "{user.email.size}")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Size(min = 0, max = 11, message = "{user.phone.size}")
    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public Date getPwdUpdateDate() {
        return pwdUpdateDate;
    }

    public void setPwdUpdateDate(Date pwdUpdateDate) {
        this.pwdUpdateDate = pwdUpdateDate;
    }

    public SysDept getDept() {
        return dept;
    }

    public void setDept(SysDept dept) {
        this.dept = dept;
    }

    public List<SysRole> getRoles() {
        return roles;
    }

    public void setRoles(List<SysRole> roles) {
        this.roles = roles;
    }

    public Long[] getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Long[] roleIds) {
        this.roleIds = roleIds;
    }

    public Long[] getPostIds() {
        return postIds;
    }

    public void setPostIds(Long[] postIds) {
        this.postIds = postIds;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("userId", getUserId())
                .append("deptId", getDeptId())
                .append("userName", getUserName())
                .append("nickName", getNickName())
                .append("email", getEmail())
                .append("phonenumber", getPhonenumber())
                .append("sex", getSex())
                .append("avatar", getAvatar())
                .append("password", getPassword())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("loginIp", getLoginIp())
                .append("loginDate", getLoginDate())
                .append("pwdUpdateDate", getPwdUpdateDate())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .append("dept", getDept())
                .toString();
    }
}

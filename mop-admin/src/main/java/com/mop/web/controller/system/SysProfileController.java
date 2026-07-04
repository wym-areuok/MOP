package com.mop.web.controller.system;

import com.mop.common.annotation.Log;
import com.mop.common.config.RuoYiConfig;
import com.mop.common.core.controller.BaseController;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.core.domain.entity.SysUser;
import com.mop.common.core.domain.model.LoginUser;
import com.mop.common.enums.BusinessType;
import com.mop.common.utils.DateUtils;
import com.mop.common.utils.SecurityUtils;
import com.mop.common.utils.StringUtils;
import com.mop.common.utils.file.FileUploadUtils;
import com.mop.common.utils.file.FileUtils;
import com.mop.common.utils.file.MimeTypeUtils;
import com.mop.framework.web.service.TokenService;
import com.mop.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 个人信息 业务处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(SysProfileController.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    /**
     * 个人信息
     */
    @GetMapping
    public AjaxResult profile() {
        LoginUser loginUser = getLoginUser();
        SysUser user = loginUser.getUser();
        AjaxResult ajax = AjaxResult.success(user);
        ajax.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
        ajax.put("postGroup", userService.selectUserPostGroup(loginUser.getUsername()));
        return ajax;
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateProfile(@RequestBody Map<String, Object> params) {
        try {
            LoginUser loginUser = getLoginUser();
            SysUser currentUser = loginUser.getUser();
            String nickName = (String) params.get("nickName");
            String email = (String) params.get("email");
            String phonenumber = (String) params.get("phonenumber");
            String sex = (String) params.get("sex");
            if (StringUtils.isEmpty(nickName)) {
                return error("用户昵称不能为空");
            }
            currentUser.setNickName(nickName);
            currentUser.setEmail(email);
            currentUser.setPhonenumber(phonenumber);
            currentUser.setSex(sex);
            if (StringUtils.isNotEmpty(phonenumber) && !userService.checkPhoneUnique(currentUser)) {
                return error("修改用户'" + loginUser.getUsername() + "'失败，手机号码已存在");
            }
            if (StringUtils.isNotEmpty(email) && !userService.checkEmailUnique(currentUser)) {
                return error("修改用户'" + loginUser.getUsername() + "'失败，邮箱账号已存在");
            }
            if (userService.updateUserProfile(currentUser) > 0) {
                // 更新缓存用户信息
                tokenService.setLoginUser(loginUser);
                return success();
            }
            return error("修改个人信息异常，请联系管理员");
        } catch (Exception e) {
            log.error("修改个人信息失败", e);
            return error("修改个人信息异常，请联系管理员");
        }
    }

    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody Map<String, String> params) {
        try {
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            if (StringUtils.isEmpty(oldPassword)) {
                return error("旧密码不能为空");
            }
            if (StringUtils.isEmpty(newPassword)) {
                return error("新密码不能为空");
            }
            if (newPassword.length() < 6 || newPassword.length() > 20) {
                return error("新密码长度需在6-20个字符之间");
            }
            LoginUser loginUser = getLoginUser();
            Long userId = loginUser.getUserId();
            SysUser user = userService.selectUserById(userId);
            String password = user.getPassword();
            if (!SecurityUtils.matchesPassword(oldPassword, password)) {
                return error("修改密码失败，旧密码错误");
            }
            if (SecurityUtils.matchesPassword(newPassword, password)) {
                return error("新密码不能与旧密码相同");
            }
            newPassword = SecurityUtils.encryptPassword(newPassword);
            if (userService.resetUserPwd(userId, newPassword) > 0) {
                // 更新缓存用户密码&密码最后更新时间
                loginUser.getUser().setPwdUpdateDate(DateUtils.getNowDate());
                loginUser.getUser().setPassword(newPassword);
                tokenService.setLoginUser(loginUser);
                return success();
            }
            return error("修改密码异常，请联系管理员");
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return error("修改密码异常，请联系管理员");
        }
    }

    /**
     * 头像上传
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                LoginUser loginUser = getLoginUser();
                String avatar = FileUploadUtils.upload(RuoYiConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION, true, FileUploadUtils.AVATAR_MAX_SIZE);
                if (userService.updateUserAvatar(loginUser.getUserId(), avatar)) {
                    String oldAvatar = loginUser.getUser().getAvatar();
                    if (StringUtils.isNotEmpty(oldAvatar)) {
                        FileUtils.deleteFile(RuoYiConfig.getProfile() + FileUtils.stripPrefix(oldAvatar));
                    }
                    AjaxResult ajax = AjaxResult.success();
                    ajax.put("imgUrl", avatar);
                    // 更新缓存用户头像
                    loginUser.getUser().setAvatar(avatar);
                    tokenService.setLoginUser(loginUser);
                    return ajax;
                }
            }
            return error("上传图片异常，请联系管理员");
        } catch (Exception e) {
            log.error("上传头像失败", e);
            return error("上传图片异常，请联系管理员");
        }
    }
}

package com.mop.web.controller.system;

import com.mop.common.annotation.Log;
import com.mop.common.annotation.RateLimiter;
import com.mop.common.config.MopConfig;
import com.mop.common.core.controller.BaseController;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.core.domain.entity.SysUser;
import com.mop.common.core.domain.model.LoginUser;
import com.mop.common.enums.BusinessType;
import com.mop.common.utils.DateUtils;
import com.mop.common.utils.MessageUtils;
import com.mop.common.utils.SecurityUtils;
import com.mop.common.utils.StringUtils;
import com.mop.common.utils.file.FileUploadUtils;
import com.mop.common.utils.file.FileUtils;
import com.mop.common.utils.file.MimeTypeUtils;
import com.mop.framework.web.service.TokenService;
import com.mop.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 个人信息 业务处理
 *
 * @author weiyiming
 */
@Tag(name = "个人信息")
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(SysProfileController.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    @Operation(summary = "获取个人基本信息")
    @GetMapping
    public AjaxResult profile() {
        LoginUser loginUser = getLoginUser();
        SysUser user = loginUser.getUser();
        AjaxResult ajax = AjaxResult.success(user);
        ajax.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
        ajax.put("postGroup", userService.selectUserPostGroup(loginUser.getUsername()));
        return ajax;
    }

    @Operation(summary = "修改个人信息")
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
                return error(MessageUtils.message("profile.nickname.not.empty"));
            }
            currentUser.setNickName(nickName);
            currentUser.setEmail(email);
            currentUser.setPhonenumber(phonenumber);
            currentUser.setSex(sex);
            if (StringUtils.isNotEmpty(phonenumber) && !userService.checkPhoneUnique(currentUser)) {
                return error(MessageUtils.message("profile.update.fail.phone.exists", loginUser.getUsername()));
            }
            if (StringUtils.isNotEmpty(email) && !userService.checkEmailUnique(currentUser)) {
                return error(MessageUtils.message("profile.update.fail.email.exists", loginUser.getUsername()));
            }
            if (userService.updateUserProfile(currentUser) > 0) {
                // 更新缓存用户信息
                tokenService.setLoginUser(loginUser);
                return success();
            }
            return error(MessageUtils.message("profile.update.fail"));
        } catch (Exception e) {
            log.error("修改个人信息失败", e);
            return error(MessageUtils.message("profile.update.fail"));
        }
    }

    @Operation(summary = "修改个人密码")
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody Map<String, String> params) {
        try {
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            if (StringUtils.isEmpty(oldPassword)) {
                return error(MessageUtils.message("profile.password.old.not.empty"));
            }
            if (StringUtils.isEmpty(newPassword)) {
                return error(MessageUtils.message("profile.password.new.not.empty"));
            }
            if (newPassword.length() < 6 || newPassword.length() > 20) {
                return error(MessageUtils.message("profile.password.length.invalid"));
            }
            LoginUser loginUser = getLoginUser();
            Long userId = loginUser.getUserId();
            SysUser user = userService.selectUserById(userId);
            if (StringUtils.isNull(user)) {
                return error(MessageUtils.message("user.not.exists"));
            }
            String password = user.getPassword();
            if (!SecurityUtils.matchesPassword(oldPassword, password)) {
                return error(MessageUtils.message("profile.password.old.wrong"));
            }
            if (SecurityUtils.matchesPassword(newPassword, password)) {
                return error(MessageUtils.message("profile.password.same"));
            }
            newPassword = SecurityUtils.encryptPassword(newPassword);
            if (userService.resetUserPwd(userId, newPassword) > 0) {
                // 更新缓存用户密码&密码最后更新时间
                loginUser.getUser().setPwdUpdateDate(DateUtils.getNowDate());
                loginUser.getUser().setPassword(newPassword);
                tokenService.setLoginUser(loginUser);
                return success();
            }
            return error(MessageUtils.message("profile.password.update.fail"));
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return error(MessageUtils.message("profile.password.update.fail"));
        }
    }

    @Operation(summary = "上传用户头像")
    @RateLimiter(key = "avatar", count = 3, time = 60)
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                LoginUser loginUser = getLoginUser();
                String avatar = FileUploadUtils.upload(MopConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION, true, FileUploadUtils.AVATAR_MAX_SIZE);
                if (userService.updateUserAvatar(loginUser.getUserId(), avatar)) {
                    String oldAvatar = loginUser.getUser().getAvatar();
                    if (StringUtils.isNotEmpty(oldAvatar)) {
                        FileUtils.deleteFile(MopConfig.getProfile() + FileUtils.stripPrefix(oldAvatar));
                    }
                    AjaxResult ajax = AjaxResult.success();
                    ajax.put("imgUrl", avatar);
                    // 更新缓存用户头像
                    loginUser.getUser().setAvatar(avatar);
                    tokenService.setLoginUser(loginUser);
                    return ajax;
                }
            }
            return error(MessageUtils.message("profile.avatar.upload.fail"));
        } catch (Exception e) {
            log.error("上传头像失败", e);
            return error(MessageUtils.message("profile.avatar.upload.fail"));
        }
    }
}

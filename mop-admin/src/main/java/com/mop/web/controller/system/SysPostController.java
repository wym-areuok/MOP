package com.mop.web.controller.system;

import com.mop.common.annotation.Log;
import com.mop.common.core.controller.BaseController;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.core.page.TableDataInfo;
import com.mop.common.enums.BusinessType;
import com.mop.common.utils.MessageUtils;
import com.mop.common.utils.poi.ExcelUtil;
import com.mop.system.domain.SysPost;
import com.mop.system.service.ISysPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位信息操作处理
 *
 * @author weiyiming
 */
@Tag(name = "岗位管理")
@RestController
@RequestMapping("/system/post")
public class SysPostController extends BaseController {
    @Autowired
    private ISysPostService postService;

    @Operation(summary = "查询岗位列表")
    @PreAuthorize("@ss.hasPermi('system:post:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysPost post) {
        startPage();
        List<SysPost> list = postService.selectPostList(post);
        return getDataTable(list);
    }

    @Operation(summary = "导出岗位数据")
    @Log(title = "岗位管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:post:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysPost post) {
        List<SysPost> list = postService.selectPostList(post);
        ExcelUtil<SysPost> util = new ExcelUtil<SysPost>(SysPost.class);
        util.exportExcel(response, list, MessageUtils.message("post.export.title"));
    }

    @Operation(summary = "根据岗位ID获取详细信息")
    @PreAuthorize("@ss.hasPermi('system:post:query')")
    @GetMapping(value = "/{postId}")
    public AjaxResult getInfo(@Parameter(description = "岗位ID") @PathVariable Long postId) {
        return success(postService.selectPostById(postId));
    }

    @Operation(summary = "新增岗位")
    @PreAuthorize("@ss.hasPermi('system:post:add')")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysPost post) {
        if (!postService.checkPostNameUnique(post)) {
            return error(MessageUtils.message("post.add.fail.name.exists", post.getPostName()));
        } else if (!postService.checkPostCodeUnique(post)) {
            return error(MessageUtils.message("post.add.fail.code.exists", post.getPostName()));
        }
        post.setCreateBy(getUsername());
        return toAjax(postService.insertPost(post));
    }

    @Operation(summary = "修改岗位")
    @PreAuthorize("@ss.hasPermi('system:post:edit')")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysPost post) {
        if (!postService.checkPostNameUnique(post)) {
            return error(MessageUtils.message("post.update.fail.name.exists", post.getPostName()));
        } else if (!postService.checkPostCodeUnique(post)) {
            return error(MessageUtils.message("post.update.fail.code.exists", post.getPostName()));
        }
        post.setUpdateBy(getUsername());
        return toAjax(postService.updatePost(post));
    }

    @Operation(summary = "删除岗位")
    @PreAuthorize("@ss.hasPermi('system:post:remove')")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    public AjaxResult remove(@Parameter(description = "岗位ID数组") @PathVariable Long[] postIds) {
        return toAjax(postService.deletePostByIds(postIds));
    }

    @Operation(summary = "获取岗位下拉选择列表")
    @GetMapping("/optionselect")
    public AjaxResult optionselect() {
        List<SysPost> posts = postService.selectPostAll();
        return success(posts);
    }
}

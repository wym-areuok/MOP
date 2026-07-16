package com.mop.web.controller.system;

import com.mop.common.annotation.Log;
import com.mop.common.core.controller.BaseController;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.core.page.TableDataInfo;
import com.mop.common.core.text.Convert;
import com.mop.common.enums.BusinessType;
import com.mop.system.domain.SysNotice;
import com.mop.system.service.ISysNoticeReadService;
import com.mop.system.service.ISysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告 信息操作处理
 *
 * @author weiyiming
 */
@Tag(name = "通知公告")
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {
    @Autowired
    private ISysNoticeService noticeService;

    @Autowired
    private ISysNoticeReadService noticeReadService;

    @Operation(summary = "查询通知公告列表")
    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysNotice notice) {
        startPage();
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    @Operation(summary = "根据公告ID获取详细信息")
    @PreAuthorize("@ss.hasPermi('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@Parameter(description = "公告ID") @PathVariable Long noticeId) {
        return success(noticeService.selectNoticeById(noticeId));
    }

    @Operation(summary = "新增通知公告")
    @PreAuthorize("@ss.hasPermi('system:notice:add')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysNotice notice) {
        notice.setCreateBy(getUsername());
        return toAjax(noticeService.insertNotice(notice));
    }

    @Operation(summary = "修改通知公告")
    @PreAuthorize("@ss.hasPermi('system:notice:edit')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysNotice notice) {
        notice.setUpdateBy(getUsername());
        return toAjax(noticeService.updateNotice(notice));
    }

    @Operation(summary = "获取首页顶部公告（最多5条，含已读标记）")
    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    @GetMapping("/listTop")
    @ResponseBody
    public AjaxResult listTop() {
        Long userId = getUserId();
        List<SysNotice> list = noticeReadService.selectNoticeListWithReadStatus(userId, 5);
        long unreadCount = list.stream().filter(n -> !Boolean.TRUE.equals(n.getIsRead())).count();
        AjaxResult result = AjaxResult.success(list);
        result.put("unreadCount", unreadCount);
        return result;
    }

    @Operation(summary = "标记公告已读")
    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    @PostMapping("/markRead")
    @ResponseBody
    public AjaxResult markRead(Long noticeId) {
        Long userId = getUserId();
        noticeReadService.markRead(noticeId, userId);
        return success();
    }

    @Operation(summary = "批量标记公告已读")
    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    @PostMapping("/markReadAll")
    @ResponseBody
    public AjaxResult markReadAll(String ids) {
        Long userId = getUserId();
        Long[] noticeIds = Convert.toLongArray(ids);
        noticeReadService.markReadBatch(userId, noticeIds);
        return success();
    }

    @Operation(summary = "查询公告已读用户列表")
    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    @GetMapping("/readUsers/list")
    @ResponseBody
    public TableDataInfo readUsersList(Long noticeId, String searchValue) {
        startPage();
        List<?> list = noticeReadService.selectReadUsersByNoticeId(noticeId, searchValue);
        return getDataTable(list);
    }

    @Operation(summary = "删除通知公告")
    @PreAuthorize("@ss.hasPermi('system:notice:remove')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@Parameter(description = "公告ID数组") @PathVariable Long[] noticeIds) {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }
}

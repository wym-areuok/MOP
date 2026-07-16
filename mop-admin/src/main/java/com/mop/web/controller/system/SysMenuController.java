package com.mop.web.controller.system;

import com.mop.common.annotation.Log;
import com.mop.common.constant.UserConstants;
import com.mop.common.core.controller.BaseController;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.core.domain.entity.SysMenu;
import com.mop.common.enums.BusinessType;
import com.mop.common.utils.MessageUtils;
import com.mop.common.utils.StringUtils;
import com.mop.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 菜单信息
 *
 * @author weiyiming
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
public class SysMenuController extends BaseController {
    @Autowired
    private ISysMenuService menuService;

    @Operation(summary = "查询菜单列表")
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/list")
    public AjaxResult list(SysMenu menu) {
        List<SysMenu> menus = menuService.selectMenuList(menu, getUserId());
        return success(menus);
    }

    @Operation(summary = "根据菜单ID获取详细信息")
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public AjaxResult getInfo(@Parameter(description = "菜单ID") @PathVariable Long menuId) {
        return success(menuService.selectMenuById(menuId));
    }

    @Operation(summary = "获取菜单树形下拉列表")
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/treeselect")
    public AjaxResult treeselect(SysMenu menu) {
        List<SysMenu> menus = menuService.selectMenuList(menu, getUserId());
        return success(menuService.buildMenuTreeSelect(menus));
    }

    @Operation(summary = "加载角色菜单树（含已选节点）")
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public AjaxResult roleMenuTreeselect(@Parameter(description = "角色ID") @PathVariable("roleId") Long roleId) {
        List<SysMenu> menus = menuService.selectMenuList(getUserId());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
        ajax.put("menus", menuService.buildMenuTreeSelect(menus));
        return ajax;
    }

    @Operation(summary = "新增菜单")
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysMenu menu) {
        if (!menuService.checkMenuNameUnique(menu)) {
            return error(MessageUtils.message("menu.add.fail.name.exists", menu.getMenuName()));
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            return error(MessageUtils.message("menu.add.fail.url.invalid", menu.getMenuName()));
        } else if (!menuService.checkRouteConfigUnique(menu)) {
            return error(MessageUtils.message("menu.add.fail.route.exists", menu.getMenuName()));
        }
        menu.setCreateBy(getUsername());
        return toAjax(menuService.insertMenu(menu));
    }

    @Operation(summary = "修改菜单")
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysMenu menu) {
        if (!menuService.checkMenuNameUnique(menu)) {
            return error(MessageUtils.message("menu.update.fail.name.exists", menu.getMenuName()));
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            return error(MessageUtils.message("menu.update.fail.url.invalid", menu.getMenuName()));
        } else if (menu.getMenuId().equals(menu.getParentId())) {
            return error(MessageUtils.message("menu.update.fail.parent.invalid", menu.getMenuName()));
        } else if (!menuService.checkRouteConfigUnique(menu)) {
            return error(MessageUtils.message("menu.update.fail.route.exists", menu.getMenuName()));
        }
        menu.setUpdateBy(getUsername());
        return toAjax(menuService.updateMenu(menu));
    }

    @Operation(summary = "保存菜单排序")
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @Log(title = "保存菜单排序", businessType = BusinessType.UPDATE)
    @PutMapping("/updateSort")
    public AjaxResult updateSort(@RequestBody Map<String, String> params) {
        String[] menuIds = params.get("menuIds").split(",");
        String[] orderNums = params.get("orderNums").split(",");
        menuService.updateMenuSort(menuIds, orderNums);
        return success();
    }

    @Operation(summary = "删除菜单")
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public AjaxResult remove(@Parameter(description = "菜单ID") @PathVariable("menuId") Long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            return warn(MessageUtils.message("menu.delete.child.exists"));
        }
        if (menuService.checkMenuExistRole(menuId)) {
            return warn(MessageUtils.message("menu.delete.assigned"));
        }
        return toAjax(menuService.deleteMenuById(menuId));
    }
}
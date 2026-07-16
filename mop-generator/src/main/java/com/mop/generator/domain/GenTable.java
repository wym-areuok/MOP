package com.mop.generator.domain;

import com.mop.common.constant.GenConstants;
import com.mop.common.core.domain.BaseEntity;
import com.mop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * 业务表 gen_table
 * <p>
 * 字段描述与 mop_initial.sql 中 gen_table 表 MS_Description 保持一致。
 *
 * @author weiyiming
 */
@Schema(description = "代码生成业务表")
public class GenTable extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Schema(description = "编号", example = "1")
    private Long tableId;

    @Schema(description = "表名称", required = true, example = "sys_user")
    @NotBlank(message = "{gen.table.name.not.blank}")
    private String tableName;

    @Schema(description = "表描述", required = true, example = "用户信息表")
    @NotBlank(message = "{gen.table.desc.not.blank}")
    private String tableComment;

    @Schema(description = "关联子表的表名")
    private String subTableName;

    @Schema(description = "子表关联的外键名")
    private String subTableFkName;

    @Schema(description = "实体类名称", required = true, example = "SysUser")
    @NotBlank(message = "{gen.table.class.name.not.blank}")
    private String className;

    @Schema(description = "使用的模板（crud=单表 tree=树表 sub=主子表）", allowableValues = {"crud", "tree", "sub"}, example = "crud")
    private String tplCategory;

    @Schema(description = "前端模板类型（element-plus模版）", example = "element-plus")
    private String tplWebType;

    @Schema(description = "生成包路径", required = true, example = "com.mop.system")
    @NotBlank(message = "{gen.table.package.not.blank}")
    private String packageName;

    @Schema(description = "生成模块名", required = true, example = "system")
    @NotBlank(message = "{gen.table.module.not.blank}")
    private String moduleName;

    @Schema(description = "生成业务名", required = true, example = "user")
    @NotBlank(message = "{gen.table.business.not.blank}")
    private String businessName;

    @Schema(description = "生成功能名", required = true, example = "用户")
    @NotBlank(message = "{gen.table.function.not.blank}")
    private String functionName;

    @Schema(description = "生成功能作者", required = true, example = "weiyiming")
    @NotBlank(message = "{gen.table.author.not.blank}")
    private String functionAuthor;

    @Schema(description = "表单布局（1=单列 2=双列 3=三列）", example = "1")
    private Integer formColNum;

    @Schema(description = "生成代码方式（0=zip压缩包 1=自定义路径）", allowableValues = {"0", "1"}, example = "0")
    private String genType;

    @Schema(description = "生成路径")
    private String genPath;

    @Schema(description = "主键信息", accessMode = Schema.AccessMode.READ_ONLY)
    private GenTableColumn pkColumn;

    @Schema(description = "子表信息", accessMode = Schema.AccessMode.READ_ONLY)
    private GenTable subTable;

    @Schema(description = "表列信息")
    @Valid
    private List<GenTableColumn> columns;

    @Schema(description = "其它生成选项")
    private String options;

    @Schema(description = "树编码字段")
    private String treeCode;

    @Schema(description = "树父编码字段")
    private String treeParentCode;

    @Schema(description = "树名称字段")
    private String treeName;

    @Schema(description = "上级菜单ID字段")
    private Long parentMenuId;

    @Schema(description = "上级菜单名称字段")
    private String parentMenuName;

    @Schema(description = "是否生成详情页")
    private boolean isView;

    public static boolean isSub(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_SUB, tplCategory);
    }

    public static boolean isTree(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_TREE, tplCategory);
    }

    public static boolean isCrud(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_CRUD, tplCategory);
    }

    public static boolean isSuperColumn(String tplCategory, String javaField) {
        if (isTree(tplCategory)) {
            return StringUtils.equalsAnyIgnoreCase(javaField,
                    ArrayUtils.addAll(GenConstants.TREE_ENTITY, GenConstants.BASE_ENTITY));
        }
        return StringUtils.equalsAnyIgnoreCase(javaField, GenConstants.BASE_ENTITY);
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public String getSubTableName() {
        return subTableName;
    }

    public void setSubTableName(String subTableName) {
        this.subTableName = subTableName;
    }

    public String getSubTableFkName() {
        return subTableFkName;
    }

    public void setSubTableFkName(String subTableFkName) {
        this.subTableFkName = subTableFkName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTplCategory() {
        return tplCategory;
    }

    public void setTplCategory(String tplCategory) {
        this.tplCategory = tplCategory;
    }

    public String getTplWebType() {
        return tplWebType;
    }

    public void setTplWebType(String tplWebType) {
        this.tplWebType = tplWebType;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionAuthor() {
        return functionAuthor;
    }

    public void setFunctionAuthor(String functionAuthor) {
        this.functionAuthor = functionAuthor;
    }

    public Integer getFormColNum() {
        return formColNum;
    }

    public void setFormColNum(Integer formColNum) {
        this.formColNum = formColNum;
    }

    public String getGenType() {
        return genType;
    }

    public void setGenType(String genType) {
        this.genType = genType;
    }

    public String getGenPath() {
        return genPath;
    }

    public void setGenPath(String genPath) {
        this.genPath = genPath;
    }

    public GenTableColumn getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(GenTableColumn pkColumn) {
        this.pkColumn = pkColumn;
    }

    public GenTable getSubTable() {
        return subTable;
    }

    public void setSubTable(GenTable subTable) {
        this.subTable = subTable;
    }

    public List<GenTableColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<GenTableColumn> columns) {
        this.columns = columns;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getTreeCode() {
        return treeCode;
    }

    public void setTreeCode(String treeCode) {
        this.treeCode = treeCode;
    }

    public String getTreeParentCode() {
        return treeParentCode;
    }

    public void setTreeParentCode(String treeParentCode) {
        this.treeParentCode = treeParentCode;
    }

    public String getTreeName() {
        return treeName;
    }

    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }

    public Long getParentMenuId() {
        return parentMenuId;
    }

    public void setParentMenuId(Long parentMenuId) {
        this.parentMenuId = parentMenuId;
    }

    public String getParentMenuName() {
        return parentMenuName;
    }

    public void setParentMenuName(String parentMenuName) {
        this.parentMenuName = parentMenuName;
    }

    public boolean isView() {
        return isView;
    }

    public void setView(boolean isView) {
        this.isView = isView;
    }

    public boolean isSub() {
        return isSub(this.tplCategory);
    }

    public boolean isTree() {
        return isTree(this.tplCategory);
    }

    public boolean isCrud() {
        return isCrud(this.tplCategory);
    }

    public boolean isSuperColumn(String javaField) {
        return isSuperColumn(this.tplCategory, javaField);
    }
}
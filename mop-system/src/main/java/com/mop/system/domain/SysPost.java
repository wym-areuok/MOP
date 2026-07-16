package com.mop.system.domain;

import com.mop.common.annotation.Excel;
import com.mop.common.annotation.Excel.ColumnType;
import com.mop.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 岗位表 sys_post
 * <p>
 * 字段描述与 mop_initial.sql 中 sys_post 表 MS_Description 保持一致。
 *
 * @author weiyiming
 */
@Schema(description = "系统岗位")
public class SysPost extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Schema(description = "岗位ID", example = "1")
    @Excel(name = "岗位序号", cellType = ColumnType.NUMERIC)
    private Long postId;

    @Schema(description = "岗位编码", required = true, example = "ceo")
    @Excel(name = "岗位编码")
    private String postCode;

    @Schema(description = "岗位名称", required = true, example = "董事长")
    @Excel(name = "岗位名称")
    private String postName;

    @Schema(description = "显示顺序", example = "1")
    @Excel(name = "岗位排序")
    private Integer postSort;

    @Schema(description = "状态（0=正常 1=停用）", allowableValues = {"0", "1"}, example = "0")
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    @Schema(description = "用户是否已分配此岗位", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean flag = false;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    @NotBlank(message = "{post.code.not.blank}")
    @Size(min = 0, max = 64, message = "{post.code.size}")
    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    @NotBlank(message = "{post.name.not.blank}")
    @Size(min = 0, max = 50, message = "{post.name.size}")
    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    @NotNull(message = "{post.sort.not.null}")
    public Integer getPostSort() {
        return postSort;
    }

    public void setPostSort(Integer postSort) {
        this.postSort = postSort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("postId", getPostId())
                .append("postCode", getPostCode())
                .append("postName", getPostName())
                .append("postSort", getPostSort())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

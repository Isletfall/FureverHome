package cn.fzu.edu.furever_home.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通用分页返回结构")
public class PageResult<T> {

    @Schema(description = "当前页号，从1开始")
    private long page;

    @Schema(description = "每页数量")
    private long pageSize;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "数据列表")
    private List<T> records;

    public static <T> PageResult<T> empty(long page, long pageSize) {
        return new PageResult<>(page, pageSize, 0, Collections.emptyList());
    }
}


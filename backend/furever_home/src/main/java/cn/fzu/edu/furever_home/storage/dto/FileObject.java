package cn.fzu.edu.furever_home.storage.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(name = "文件对象", description = "用于返回完整文件数据")
public class FileObject {
    @Schema(description = "内容类型")
    private String contentType;
    @Schema(description = "文件数据")
    private byte[] data;
}
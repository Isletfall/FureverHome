package cn.fzu.edu.furever_home.storage.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(name = "上传结果", description = "文件上传后的结果信息")
public class UploadResult {
    @Schema(description = "存储桶")
    private String bucket;
    @Schema(description = "对象名")
    private String objectName;
    @Schema(description = "访问URL")
    private String url;
}
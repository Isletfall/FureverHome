package cn.fzu.edu.furever_home.storage.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.InputStream;

@Data
@Schema(name = "文件流对象", description = "用于分段流式传输文件")
public class StreamObject {
    @Schema(description = "内容类型")
    private String contentType;
    @Schema(description = "文件总长度")
    private long totalLength;
    @Schema(description = "起始字节")
    private long start;
    @Schema(description = "本次内容长度")
    private long contentLength;
    @Schema(description = "输入流")
    private InputStream inputStream;
}
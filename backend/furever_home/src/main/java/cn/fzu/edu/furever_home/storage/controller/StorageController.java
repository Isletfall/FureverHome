package cn.fzu.edu.furever_home.storage.controller;

import cn.fzu.edu.furever_home.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.fzu.edu.furever_home.common.result.Result;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Tag(name = "存储服务", description = "图片/视频上传、查看、删除")
public class StorageController {
    private final StorageService storageService;
    private final Environment env;

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传图片", description = "返回可直接查看的服务路由")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = true, example = "Bearer xxxxxx")
    public Result<String> uploadImage(@RequestPart("file") MultipartFile file) {
        String bucket = env.getProperty("minio.bucket-images", "images");
        String objectName = storageService.upload(bucket, file, null);
        String viewPath = "/api/storage/image/" + objectName;
        return Result.success(viewPath);
    }

    @PostMapping(value = "/upload/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传视频", description = "返回可直接查看的服务路由")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = true, example = "Bearer xxxxxx")
    public Result<String> uploadVideo(@RequestPart("file") MultipartFile file) {
        long max = 100L * 1024 * 1024;
        if (file.getSize() > max) {
            return Result.error(400, "视频大小超过100MB");
        }
        String bucket = env.getProperty("minio.bucket-videos", "videos");
        String objectName = storageService.upload(bucket, file, null);
        String viewPath = "/api/storage/video/" + objectName;
        return Result.success(viewPath);
    }

    @GetMapping("/image/{object}")
    @Operation(summary = "查看图片")
    public ResponseEntity<byte[]> viewImage(@Parameter(description = "图片对象名") @PathVariable("object") String objectName) {
        String bucket = env.getProperty("minio.bucket-images", "images");
        cn.fzu.edu.furever_home.storage.dto.FileObject f = storageService.getFile(bucket, objectName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(f.getData().length))
                .contentType(MediaType
                        .parseMediaType(f.getContentType() == null ? MediaType.IMAGE_JPEG_VALUE : f.getContentType()))
                .body(f.getData());
    }

    @GetMapping("/video/{object}")
    @Operation(summary = "查看视频（支持分段）")
    public ResponseEntity<StreamingResponseBody> viewVideo(@Parameter(description = "视频对象名") @PathVariable("object") String objectName,
            @RequestHeader(value = "Range", required = false) String range, HttpServletResponse response) {
        String bucket = env.getProperty("minio.bucket-videos", "videos");
        Long start = null;
        Long end = null;
        if (range != null && range.startsWith("bytes=")) {
            String r = range.substring(6);
            String[] se = r.split("-");
            try {
                if (se.length > 0 && !se[0].isEmpty())
                    start = Long.parseLong(se[0]);
                if (se.length > 1 && !se[1].isEmpty())
                    end = Long.parseLong(se[1]);
            } catch (Exception ignored) {
            }
        }
        Long length = null;
        if (start == null && end != null) {
            length = end;
            start = null;
        } else if (start != null && end != null && end >= start) {
            length = end - start + 1;
        }
        cn.fzu.edu.furever_home.storage.dto.StreamObject s = storageService.getStream(bucket, objectName, start,
                length);
        String ct = s.getContentType() == null ? "video/mp4" : s.getContentType();
        int bufSizeConfig = env.getProperty("storage.stream.buffer-size", Integer.class, 104857600);
        int maxCap = 100 * 1024 * 1024;
        final int bufSize = Math.min(Math.max(bufSizeConfig, 8192), maxCap);
        response.setBufferSize(bufSize);
        StreamingResponseBody body = outputStream -> {
            try (java.io.InputStream is = s.getInputStream()) {
                byte[] buf = new byte[bufSize];
                int n;
                long remain = s.getContentLength();
                while (remain > 0 && (n = is.read(buf, 0, (int) Math.min(buf.length, remain))) != -1) {
                    outputStream.write(buf, 0, n);
                    remain -= n;
                }
            }
        };
        if (start != null) {
            long from = s.getStart();
            long to = s.getStart() + s.getContentLength() - 1;
            return ResponseEntity.status(206)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + from + "-" + to + "/" + s.getTotalLength())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(s.getContentLength()))
                    .contentType(MediaType.parseMediaType(ct))
                    .body(body);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(s.getContentLength()))
                .contentType(MediaType.parseMediaType(ct))
                .body(body);
    }

    @DeleteMapping("/image/{object}")
    @Operation(summary = "删除图片")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = true, example = "Bearer xxxxxx")
    public Result<Void> deleteImage(@Parameter(description = "图片对象名") @PathVariable("object") String objectName) {
        String bucket = env.getProperty("minio.bucket-images", "images");
        storageService.delete(bucket, objectName);
        return Result.success();
    }

    @DeleteMapping("/video/{object}")
    @Operation(summary = "删除视频")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = true, example = "Bearer xxxxxx")
    public Result<Void> deleteVideo(@Parameter(description = "视频对象名") @PathVariable("object") String objectName) {
        String bucket = env.getProperty("minio.bucket-videos", "videos");
        storageService.delete(bucket, objectName);
        return Result.success();
    }
}
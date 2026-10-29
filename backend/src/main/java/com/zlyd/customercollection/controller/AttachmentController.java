package com.zlyd.customercollection.controller;

import com.zlyd.customercollection.common.Result;
import com.zlyd.customercollection.common.UserContext;
import com.zlyd.customercollection.dto.AttachmentBindRequest;
import com.zlyd.customercollection.entity.Attachment;
import com.zlyd.customercollection.service.AttachmentService;
import com.zlyd.customercollection.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public Result<Attachment> upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam Integer bizType,
                                     @RequestParam(defaultValue = "0") Long bizId,
                                     @RequestParam(defaultValue = "6") Integer attachKind) {
        return Result.ok(attachmentService.upload(file, bizType, bizId, attachKind, UserContext.userId()));
    }

    @GetMapping("/list")
    public Result<List<Attachment>> list(@RequestParam Integer bizType, @RequestParam Long bizId) {
        return Result.ok(attachmentService.listByBizForUser(bizType, bizId, UserContext.userId()));
    }

    @PostMapping("/bind")
    public Result<Void> bind(@Valid @RequestBody AttachmentBindRequest request) {
        attachmentService.bind(request.getIds(), request.getBizType(), request.getBizId(), UserContext.userId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        attachmentService.delete(id, UserContext.userId());
        return Result.ok();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        return buildResponse(id, false);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(@PathVariable Long id) {
        return buildResponse(id, true);
    }

    private ResponseEntity<Resource> buildResponse(Long id, boolean inline) {
        Attachment attachment = attachmentService.getForDownload(id, UserContext.userId());
        Path path = fileStorageService.resolve(attachment.getFileKey());
        if (!Files.exists(path)) {
            throw new com.zlyd.customercollection.common.BusinessException("文件不存在或已被清理");
        }
        Resource resource = new FileSystemResource(path);
        String encodedName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        String disposition = (inline ? "inline" : "attachment") + "; filename*=UTF-8''" + encodedName;
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(attachment.getMimeType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(mediaType)
                .contentLength(attachment.getFileSize() == null ? 0 : attachment.getFileSize())
                .body(resource);
    }
}

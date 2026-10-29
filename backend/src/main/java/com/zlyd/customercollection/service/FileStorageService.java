package com.zlyd.customercollection.service;

import com.zlyd.customercollection.common.BusinessException;
import com.zlyd.customercollection.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar");
    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final AppProperties properties;

    public StoredFile store(MultipartFile file, boolean publicAccess) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        String originalName = normalizeOriginalName(file.getOriginalFilename());
        String extension = resolveExtension(originalName);
        boolean image = IMAGE_EXTENSIONS.contains(extension);
        if (!image && !DOCUMENT_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的文件类型");
        }
        long maxSize = image ? properties.getFile().getMaxImageSize() : properties.getFile().getMaxFileSize();
        if (file.getSize() > maxSize) {
            throw new BusinessException(image ? "图片不能超过10MB" : "文件不能超过50MB");
        }
        if (image) {
            verifyImageHeader(file, extension);
        }

        String relativeDir = (publicAccess ? "public/" : "private/") + LocalDate.now().format(DATE_PATH);
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String fileKey = relativeDir + "/" + storedName;
        Path root = Path.of(properties.getFile().getRoot()).toAbsolutePath().normalize();
        Path target = root.resolve(fileKey).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException("文件路径不合法");
        }

        String md5 = copyWithMd5(file, target);
        String thumbKey = null;
        if (image) {
            thumbKey = relativeDir + "/" + UUID.randomUUID().toString().replace("-", "") + "_thumb." + extension;
            createThumbnail(target, root.resolve(thumbKey));
        }

        String prefix = properties.getFile().getUrlPrefix();
        String publicUrlPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
        String fileUrl = publicAccess ? publicUrlPrefix + fileKey : null;
        String thumbUrl = publicAccess && thumbKey != null ? publicUrlPrefix + thumbKey : null;
        return new StoredFile(fileKey, fileUrl, thumbKey, thumbUrl, extension,
                file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                file.getSize(), md5, image);
    }

    public Path resolve(String fileKey) {
        Path root = Path.of(properties.getFile().getRoot()).toAbsolutePath().normalize();
        Path path = root.resolve(fileKey).normalize();
        if (!path.startsWith(root)) {
            throw new BusinessException("文件路径不合法");
        }
        return path;
    }

    private String normalizeOriginalName(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "file";
        }
        String name = originalFilename.replace("\\", "/");
        int index = name.lastIndexOf('/');
        if (index >= 0) {
            name = name.substring(index + 1);
        }
        return StringUtils.hasText(name) ? name : "file";
    }

    private String resolveExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            throw new BusinessException("文件缺少扩展名");
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private void verifyImageHeader(MultipartFile file, String extension) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(12);
            boolean valid = switch (extension) {
                case "jpg", "jpeg" -> header.length >= 3
                        && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF;
                case "png" -> header.length >= 4
                        && (header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47;
                case "gif" -> header.length >= 4
                        && header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46;
                case "bmp" -> header.length >= 2 && header[0] == 0x42 && header[1] == 0x4D;
                case "webp" -> header.length >= 12
                        && header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46;
                default -> false;
            };
            if (!valid) {
                throw new BusinessException("图片内容与扩展名不匹配，请重新选择");
            }
        } catch (IOException e) {
            throw new BusinessException("图片校验失败，请重试");
        }
    }

    private String copyWithMd5(MultipartFile file, Path target) {
        try {
            Files.createDirectories(target.getParent());
            MessageDigest digest = MessageDigest.getInstance("MD5");
            try (InputStream inputStream = new DigestInputStream(file.getInputStream(), digest)) {
                Files.copy(inputStream, target);
            }
            StringBuilder builder = new StringBuilder();
            for (byte b : digest.digest()) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            log.error("保存文件失败: {}", target, e);
            throw new BusinessException("文件保存失败，请重试");
        }
    }

    private void createThumbnail(Path source, Path target) {
        try {
            Files.createDirectories(target.getParent());
            Thumbnails.of(source.toFile())
                    .size(360, 360)
                    .keepAspectRatio(true)
                    .outputQuality(0.85)
                    .toFile(target.toFile());
        } catch (Exception e) {
            log.warn("生成缩略图失败: {}", source, e);
        }
    }

    public record StoredFile(String fileKey, String fileUrl, String thumbKey, String thumbUrl,
                             String fileExt, String mimeType, long fileSize, String fileMd5, boolean image) {
    }
}

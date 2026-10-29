package com.zlyd.customercollection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zlyd.customercollection.common.BusinessException;
import com.zlyd.customercollection.common.Result;
import com.zlyd.customercollection.entity.Attachment;
import com.zlyd.customercollection.entity.Contract;
import com.zlyd.customercollection.entity.Customer;
import com.zlyd.customercollection.entity.CustomerProject;
import com.zlyd.customercollection.entity.VisitRecord;
import com.zlyd.customercollection.mapper.AttachmentMapper;
import com.zlyd.customercollection.mapper.ContractMapper;
import com.zlyd.customercollection.mapper.CustomerMapper;
import com.zlyd.customercollection.mapper.CustomerProjectMapper;
import com.zlyd.customercollection.mapper.VisitRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    public static final int BIZ_CUSTOMER = 1;
    public static final int BIZ_PROJECT = 2;
    public static final int BIZ_VISIT = 3;
    public static final int BIZ_CONTRACT = 4;
    public static final int BIZ_AVATAR = 5;

    public static final int KIND_IMAGE = 1;
    public static final int KIND_CONTRACT = 2;
    public static final int KIND_SUPPLEMENT = 3;
    public static final int KIND_INVOICE = 4;
    public static final int KIND_VISIT_PHOTO = 5;
    public static final int KIND_OTHER = 6;

    private final AttachmentMapper attachmentMapper;
    private final CustomerMapper customerMapper;
    private final CustomerProjectMapper projectMapper;
    private final VisitRecordMapper visitRecordMapper;
    private final ContractMapper contractMapper;
    private final FileStorageService fileStorageService;

    @Transactional
    public Attachment upload(MultipartFile file, Integer bizType, Long bizId, Integer attachKind, Long userId) {
        validateBizType(bizType);
        int kind = attachKind == null ? KIND_OTHER : attachKind;
        long targetBizId = bizId == null ? 0L : bizId;
        if (targetBizId > 0) {
            assertBizAccess(bizType, targetBizId, userId);
        }
        boolean image = kind == KIND_IMAGE || kind == KIND_VISIT_PHOTO || bizType == BIZ_AVATAR;
        if (bizType == BIZ_AVATAR && !image) {
            throw new BusinessException("头像只支持图片");
        }
        boolean publicAccess = isPublicAccess(bizType, image);
        FileStorageService.StoredFile stored = fileStorageService.store(file, publicAccess);
        if (image && !stored.image()) {
            throw new BusinessException("请上传正确的图片文件");
        }

        Attachment attachment = new Attachment();
        attachment.setBizType(bizType);
        attachment.setBizId(targetBizId);
        attachment.setAttachKind(kind);
        attachment.setFileName(sanitizeFileName(file.getOriginalFilename()));
        attachment.setFileKey(stored.fileKey());
        attachment.setFileUrl(stored.fileUrl());
        attachment.setThumbKey(stored.thumbKey());
        attachment.setThumbUrl(stored.thumbUrl());
        attachment.setFileExt(stored.fileExt());
        attachment.setMimeType(stored.mimeType());
        attachment.setFileSize(stored.fileSize());
        attachment.setFileMd5(stored.fileMd5());
        attachment.setStorageType(1);
        attachment.setAccessType(publicAccess ? 1 : 2);
        attachment.setSortNo(0);
        attachment.setIzDeleted(0);
        attachment.setCreateBy(userId);
        attachment.setCreateTime(LocalDateTime.now());
        attachmentMapper.insert(attachment);
        return attachment;
    }

    @Transactional
    public Attachment uploadAvatar(MultipartFile file, Long userId) {
        attachmentMapper.delete(Wrappers.<Attachment>lambdaQuery()
                .eq(Attachment::getBizType, BIZ_AVATAR)
                .eq(Attachment::getBizId, userId));
        return upload(file, BIZ_AVATAR, userId, KIND_IMAGE, userId);
    }

    public List<Attachment> listByBiz(Integer bizType, Long bizId) {
        return attachmentMapper.selectList(Wrappers.<Attachment>lambdaQuery()
                .eq(Attachment::getBizType, bizType)
                .eq(Attachment::getBizId, bizId)
                .orderByAsc(Attachment::getSortNo)
                .orderByAsc(Attachment::getId));
    }

    public List<Attachment> listByBizForUser(Integer bizType, Long bizId, Long userId) {
        assertBizAccess(bizType, bizId, userId);
        return listByBiz(bizType, bizId);
    }

    @Transactional
    public void bind(List<Long> ids, Integer bizType, Long bizId, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        validateBizType(bizType);
        assertBizAccess(bizType, bizId, userId);
        for (Long id : ids) {
            Attachment attachment = attachmentMapper.selectById(id);
            if (attachment == null) {
                continue;
            }
            if (!Objects.equals(attachment.getCreateBy(), userId)) {
                throw new BusinessException(Result.FORBIDDEN, "无权操作该附件");
            }
            if (!Objects.equals(attachment.getBizType(), bizType)) {
                throw new BusinessException("附件业务类型不匹配");
            }
            if (attachment.getBizId() != null && attachment.getBizId() > 0
                    && !Objects.equals(attachment.getBizId(), bizId)) {
                throw new BusinessException("附件已绑定其他业务");
            }
            attachment.setBizId(bizId);
            attachment.setUpdateBy(userId);
            attachment.setUpdateTime(LocalDateTime.now());
            attachmentMapper.updateById(attachment);
        }
    }

    public Attachment getForDownload(Long id, Long userId) {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) {
            throw new BusinessException("附件不存在");
        }
        if (!canAccess(attachment, userId)) {
            throw new BusinessException(Result.FORBIDDEN, "无权访问该附件");
        }
        return attachment;
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) {
            return;
        }
        if (!Objects.equals(attachment.getCreateBy(), userId)) {
            throw new BusinessException(Result.FORBIDDEN, "无权删除该附件");
        }
        attachmentMapper.deleteById(id);
    }

    @Transactional
    public void deleteByBiz(Integer bizType, Long bizId) {
        attachmentMapper.delete(Wrappers.<Attachment>lambdaQuery()
                .eq(Attachment::getBizType, bizType)
                .eq(Attachment::getBizId, bizId));
    }

    public String firstAvatarUrl(Long userId) {
        List<Attachment> list = listByBiz(BIZ_AVATAR, userId);
        return list.isEmpty() ? null : list.get(0).getFileUrl();
    }

    public String firstAvatarThumbUrl(Long userId) {
        List<Attachment> list = listByBiz(BIZ_AVATAR, userId);
        return list.isEmpty() ? null : list.get(0).getThumbUrl();
    }

    private boolean isPublicAccess(int bizType, boolean image) {
        if (bizType == BIZ_AVATAR) {
            return true;
        }
        return image && (bizType == BIZ_CUSTOMER || bizType == BIZ_PROJECT || bizType == BIZ_VISIT);
    }

    private boolean canAccess(Attachment attachment, Long userId) {
        if (attachment.getBizId() == null || attachment.getBizId() == 0) {
            return Objects.equals(attachment.getCreateBy(), userId);
        }
        try {
            assertBizAccess(attachment.getBizType(), attachment.getBizId(), userId);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }

    private void assertBizAccess(Integer bizType, Long bizId, Long userId) {
        if (bizType == null || bizId == null) {
            throw new BusinessException("业务参数不完整");
        }
        boolean allowed = switch (bizType) {
            case BIZ_CUSTOMER -> {
                Customer customer = customerMapper.selectById(bizId);
                yield customer != null && Objects.equals(customer.getUserId(), userId);
            }
            case BIZ_PROJECT -> {
                CustomerProject project = projectMapper.selectById(bizId);
                yield project != null && Objects.equals(project.getUserId(), userId);
            }
            case BIZ_VISIT -> {
                VisitRecord visit = visitRecordMapper.selectById(bizId);
                yield visit != null && Objects.equals(visit.getUserId(), userId);
            }
            case BIZ_CONTRACT -> {
                Contract contract = contractMapper.selectById(bizId);
                yield contract != null && Objects.equals(contract.getUserId(), userId);
            }
            case BIZ_AVATAR -> Objects.equals(bizId, userId);
            default -> false;
        };
        if (!allowed) {
            throw new BusinessException(Result.FORBIDDEN, "无权访问该业务数据");
        }
    }

    private void validateBizType(Integer bizType) {
        if (bizType == null || bizType < BIZ_CUSTOMER || bizType > BIZ_AVATAR) {
            throw new BusinessException("附件业务类型不正确");
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "未命名文件";
        }
        String name = fileName.replace("\\", "/");
        int index = name.lastIndexOf('/');
        if (index >= 0) {
            name = name.substring(index + 1);
        }
        return name.length() > 255 ? name.substring(name.length() - 255) : name;
    }
}

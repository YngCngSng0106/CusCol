package com.zlyd.customercollection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("attachment")
public class Attachment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer bizType;
    private Long bizId;
    private Integer attachKind;
    private String fileName;
    private String fileKey;
    private String fileUrl;
    private String thumbKey;
    private String thumbUrl;
    private String fileExt;
    private String mimeType;
    private Long fileSize;
    private String fileMd5;
    private Integer storageType;
    private Integer accessType;
    private Integer sortNo;
    @TableLogic
    private Integer izDeleted;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}

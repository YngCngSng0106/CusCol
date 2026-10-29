package com.zlyd.customercollection.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zlyd.customercollection.common.BusinessException;
import com.zlyd.customercollection.common.PageResult;
import com.zlyd.customercollection.common.Result;
import com.zlyd.customercollection.entity.Message;
import com.zlyd.customercollection.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;

    public PageResult<Message> page(Long userId, Integer messageType, Integer izRead, long pageNo, long pageSize) {
        IPage<Message> page = messageMapper.selectPage(new Page<>(pageNo, pageSize),
                Wrappers.<Message>lambdaQuery()
                        .eq(Message::getUserId, userId)
                        .eq(messageType != null, Message::getMessageType, messageType)
                        .eq(izRead != null, Message::getIzRead, izRead)
                        .orderByDesc(Message::getCreateTime)
                        .orderByDesc(Message::getId));
        return PageResult.of(page);
    }

    public long unreadCount(Long userId) {
        return messageMapper.selectCount(Wrappers.<Message>lambdaQuery()
                .eq(Message::getUserId, userId)
                .eq(Message::getIzRead, 0));
    }

    @Transactional
    public void markRead(Long id, Long userId) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException(Result.FORBIDDEN, "无权操作该消息");
        }
        if (message.getIzRead() != null && message.getIzRead() == 1) {
            return;
        }
        message.setIzRead(1);
        message.setReadTime(LocalDateTime.now());
        messageMapper.updateById(message);
    }

    @Transactional
    public void markAllRead(Long userId) {
        messageMapper.update(null, Wrappers.<Message>lambdaUpdate()
                .eq(Message::getUserId, userId)
                .eq(Message::getIzRead, 0)
                .set(Message::getIzRead, 1)
                .set(Message::getReadTime, LocalDateTime.now()));
    }
}

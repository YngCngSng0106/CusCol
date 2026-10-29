package com.zlyd.customercollection.controller;

import com.zlyd.customercollection.common.PageResult;
import com.zlyd.customercollection.common.Result;
import com.zlyd.customercollection.common.UserContext;
import com.zlyd.customercollection.entity.Message;
import com.zlyd.customercollection.service.MessageService;
import com.zlyd.customercollection.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ReminderService reminderService;

    @GetMapping("/page")
    public Result<PageResult<Message>> page(@RequestParam(required = false) Integer messageType,
                                            @RequestParam(required = false) Integer izRead,
                                            @RequestParam(defaultValue = "1") long pageNo,
                                            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(messageService.page(UserContext.userId(), messageType, izRead, pageNo,
                Math.min(pageSize, 100)));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(messageService.unreadCount(UserContext.userId()));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        messageService.markRead(id, UserContext.userId());
        return Result.ok();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        messageService.markAllRead(UserContext.userId());
        return Result.ok();
    }

    @PostMapping("/reminder/run")
    public Result<Integer> runReminder() {
        return Result.ok(reminderService.run());
    }
}

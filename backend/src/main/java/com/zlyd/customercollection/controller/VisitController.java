package com.zlyd.customercollection.controller;

import com.zlyd.customercollection.common.PageResult;
import com.zlyd.customercollection.common.Result;
import com.zlyd.customercollection.common.UserContext;
import com.zlyd.customercollection.dto.VisitFormRequest;
import com.zlyd.customercollection.dto.VisitVO;
import com.zlyd.customercollection.service.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/visit")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @GetMapping("/page")
    public Result<PageResult<VisitVO>> page(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDateStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDateEnd,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(visitService.page(UserContext.userId(), customerId, customerName,
                visitDateStart, visitDateEnd, pageNo, Math.min(pageSize, 100)));
    }

    @GetMapping("/customer/{customerId}")
    public Result<List<VisitVO>> listByCustomer(@PathVariable Long customerId) {
        return Result.ok(visitService.listByCustomer(customerId, UserContext.userId()));
    }

    @PostMapping
    public Result<VisitVO> create(@Valid @RequestBody VisitFormRequest request) {
        return Result.ok(visitService.create(UserContext.userId(), request));
    }

    @GetMapping("/{id}")
    public Result<VisitVO> detail(@PathVariable Long id) {
        return Result.ok(visitService.detail(id, UserContext.userId()));
    }

    @PutMapping("/{id}")
    public Result<VisitVO> update(@PathVariable Long id, @Valid @RequestBody VisitFormRequest request) {
        return Result.ok(visitService.update(id, UserContext.userId(), request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        visitService.delete(id, UserContext.userId());
        return Result.ok();
    }
}

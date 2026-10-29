package com.zlyd.customercollection.controller;

import com.zlyd.customercollection.common.PageResult;
import com.zlyd.customercollection.common.Result;
import com.zlyd.customercollection.common.UserContext;
import com.zlyd.customercollection.dto.ContractFormRequest;
import com.zlyd.customercollection.dto.ContractVO;
import com.zlyd.customercollection.service.ContractService;
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
@RequestMapping("/api/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/page")
    public Result<PageResult<ContractVO>> page(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate signDateStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate signDateEnd,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(contractService.page(UserContext.userId(), customerId, keyword, customerName,
                signDateStart, signDateEnd, paymentStatus, pageNo, Math.min(pageSize, 100)));
    }

    @GetMapping("/customer/{customerId}")
    public Result<List<ContractVO>> listByCustomer(@PathVariable Long customerId) {
        return Result.ok(contractService.listByCustomer(customerId, UserContext.userId()));
    }

    @PostMapping
    public Result<ContractVO> create(@Valid @RequestBody ContractFormRequest request) {
        return Result.ok(contractService.create(UserContext.userId(), request));
    }

    @GetMapping("/{id}")
    public Result<ContractVO> detail(@PathVariable Long id) {
        return Result.ok(contractService.detail(id, UserContext.userId()));
    }

    @PutMapping("/{id}")
    public Result<ContractVO> update(@PathVariable Long id, @Valid @RequestBody ContractFormRequest request) {
        return Result.ok(contractService.update(id, UserContext.userId(), request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contractService.delete(id, UserContext.userId());
        return Result.ok();
    }
}

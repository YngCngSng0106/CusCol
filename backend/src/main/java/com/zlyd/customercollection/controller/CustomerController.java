package com.zlyd.customercollection.controller;

import com.zlyd.customercollection.common.PageResult;
import com.zlyd.customercollection.common.Result;
import com.zlyd.customercollection.common.UserContext;
import com.zlyd.customercollection.dto.CustomerDetailVO;
import com.zlyd.customercollection.dto.CustomerFormRequest;
import com.zlyd.customercollection.dto.ProjectFormRequest;
import com.zlyd.customercollection.entity.Customer;
import com.zlyd.customercollection.entity.CustomerProject;
import com.zlyd.customercollection.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/page")
    public Result<PageResult<Customer>> page(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String customerName,
                                             @RequestParam(required = false) String contactName,
                                             @RequestParam(required = false) String contactPhone,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(required = false) Integer izValid,
                                             @RequestParam(defaultValue = "1") long pageNo,
                                             @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(customerService.page(UserContext.userId(), keyword, customerName, contactName,
                contactPhone, status, izValid, pageNo, Math.min(pageSize, 100)));
    }

    @PostMapping
    public Result<Customer> create(@Valid @RequestBody CustomerFormRequest request) {
        return Result.ok(customerService.create(UserContext.userId(), request));
    }

    @GetMapping("/{id}")
    public Result<CustomerDetailVO> detail(@PathVariable Long id) {
        return Result.ok(customerService.detail(id, UserContext.userId()));
    }

    @PutMapping("/{id}")
    public Result<Customer> update(@PathVariable Long id, @Valid @RequestBody CustomerFormRequest request) {
        return Result.ok(customerService.update(id, UserContext.userId(), request));
    }

    @PutMapping("/{id}/valid")
    public Result<Void> setValid(@PathVariable Long id, @RequestParam Integer izValid) {
        customerService.setValid(id, UserContext.userId(), izValid);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id, UserContext.userId());
        return Result.ok();
    }

    @GetMapping("/{id}/project/list")
    public Result<List<CustomerProject>> projectList(@PathVariable Long id) {
        return Result.ok(customerService.listProjects(id, UserContext.userId()));
    }

    @PostMapping("/{id}/project")
    public Result<CustomerProject> addProject(@PathVariable Long id,
                                              @Valid @RequestBody ProjectFormRequest request) {
        return Result.ok(customerService.addProject(id, UserContext.userId(), request));
    }
}

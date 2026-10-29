package com.zlyd.customercollection.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zlyd.customercollection.entity.Contract;
import com.zlyd.customercollection.entity.Customer;
import com.zlyd.customercollection.entity.CustomerProject;
import com.zlyd.customercollection.entity.User;
import com.zlyd.customercollection.entity.VisitRecord;
import com.zlyd.customercollection.mapper.ContractMapper;
import com.zlyd.customercollection.mapper.CustomerMapper;
import com.zlyd.customercollection.mapper.CustomerProjectMapper;
import com.zlyd.customercollection.mapper.UserMapper;
import com.zlyd.customercollection.mapper.VisitRecordMapper;
import com.zlyd.customercollection.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppProperties properties;
    private final UserMapper userMapper;
    private final CustomerMapper customerMapper;
    private final CustomerProjectMapper projectMapper;
    private final VisitRecordMapper visitRecordMapper;
    private final ContractMapper contractMapper;
    private final PasswordEncoder passwordEncoder;
    private final ReminderService reminderService;

    @Override
    public void run(String... args) {
        if (!properties.getDemoUser().isEnabled()) {
            return;
        }
        long userCount = userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getUsername, "demo"));
        if (userCount > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername("demo");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setPhone("13800000000");
        user.setEmail("demo@example.com");
        user.setCity("上海");
        user.setStatus(1);
        user.setIzDeleted(0);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);

        Customer first = createCustomer(user.getId(), "华东智造集团", 1, "制造业", "上海",
                "张经理", "13900000001", 2, now.minusMonths(6), now.minusMonths(8));
        createCustomer(user.getId(), "星海科技有限公司", 2, "软件与信息服务", "杭州",
                "李经理", "13900000002", 2, now.minusMonths(4), now.minusMonths(7));
        Customer third = createCustomer(user.getId(), "云帆物流有限公司", 3, "物流运输", "苏州",
                "王经理", "13900000003", 3, now.minusDays(15), now.minusMonths(5));

        CustomerProject project = new CustomerProject();
        project.setCustomerId(first.getId());
        project.setUserId(user.getId());
        project.setProjectName("智能工厂改造一期");
        project.setProjectType("数字化改造");
        project.setProjectAmount(new BigDecimal("1200000.00"));
        project.setProjectStatus(2);
        project.setStartDate(LocalDate.now().minusMonths(2));
        project.setEndDate(LocalDate.now().plusMonths(4));
        project.setProjectDesc("产线数据采集与看板建设");
        project.setIzDeleted(0);
        project.setCreateBy(user.getId());
        project.setCreateTime(now);
        project.setUpdateBy(user.getId());
        project.setUpdateTime(now);
        projectMapper.insert(project);

        VisitRecord visit = new VisitRecord();
        visit.setCustomerId(third.getId());
        visit.setUserId(user.getId());
        visit.setVisitType(1);
        visit.setVisitTime(now.minusDays(15));
        visit.setVisitLocation("客户总部");
        visit.setVisitPurpose("季度回访");
        visit.setVisitContent("沟通仓储系统使用情况，确认下季度新增网点需求。");
        visit.setVisitResult("客户意向明确，预计下月启动采购流程。");
        visit.setNextVisitTime(now.plusDays(15));
        visit.setIzDeleted(0);
        visit.setCreateBy(user.getId());
        visit.setCreateTime(now.minusDays(15));
        visit.setUpdateBy(user.getId());
        visit.setUpdateTime(now.minusDays(15));
        visitRecordMapper.insert(visit);

        Contract contract = new Contract();
        contract.setContractNo("HT2026001001");
        contract.setCustomerId(third.getId());
        contract.setUserId(user.getId());
        contract.setContractName("云帆物流仓储系统年度服务合同");
        contract.setContractAmount(new BigDecimal("300000.00"));
        contract.setSignDate(LocalDate.now().minusMonths(1));
        contract.setStartDate(LocalDate.now().minusMonths(1));
        contract.setEndDate(LocalDate.now().plusMonths(11));
        contract.setContractStatus(2);
        contract.setPaymentStatus(1);
        contract.setPaidAmount(new BigDecimal("100000.00"));
        contract.setRemark("首付款已到账");
        contract.setIzDeleted(0);
        contract.setCreateBy(user.getId());
        contract.setCreateTime(now.minusMonths(1));
        contract.setUpdateBy(user.getId());
        contract.setUpdateTime(now.minusMonths(1));
        contractMapper.insert(contract);

        try {
            int count = reminderService.run();
            log.info("演示提醒消息生成 {} 条", count);
        } catch (Exception e) {
            log.warn("演示提醒消息生成失败，可稍后手动触发", e);
        }
        log.info("演示账号已初始化：demo / 123456");
    }

    private Customer createCustomer(Long userId, String name, int level, String industry, String city,
                                    String contact, String phone, int status, LocalDateTime lastVisit, LocalDateTime createTime) {
        Customer customer = new Customer();
        customer.setUserId(userId);
        customer.setCustomerName(name);
        customer.setCustomerLevel(level);
        customer.setIndustry(industry);
        customer.setCity(city);
        customer.setAddress(city + "市示范路88号");
        customer.setContactName(contact);
        customer.setContactPhone(phone);
        customer.setSource("展会");
        customer.setStatus(status);
        customer.setIzValid(1);
        customer.setLastVisitTime(lastVisit);
        customer.setRemark("演示数据");
        customer.setIzDeleted(0);
        customer.setCreateBy(userId);
        customer.setCreateTime(createTime);
        customer.setUpdateBy(userId);
        customer.setUpdateTime(createTime);
        customerMapper.insert(customer);
        return customer;
    }
}

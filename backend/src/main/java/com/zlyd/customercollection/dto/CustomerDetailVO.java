package com.zlyd.customercollection.dto;

import com.zlyd.customercollection.entity.Attachment;
import com.zlyd.customercollection.entity.Customer;
import com.zlyd.customercollection.entity.CustomerProject;
import lombok.Data;

import java.util.List;

@Data
public class CustomerDetailVO {

    private Customer customer;
    private List<CustomerProject> projects;
    private List<VisitVO> visits;
    private List<ContractVO> contracts;
    private List<Attachment> attachments;
}

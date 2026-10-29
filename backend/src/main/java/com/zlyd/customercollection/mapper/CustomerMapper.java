package com.zlyd.customercollection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zlyd.customercollection.dto.CustomerOverdueDTO;
import com.zlyd.customercollection.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    @Select("""
            SELECT c.id AS id,
                   c.user_id AS userId,
                   c.customer_name AS customerName,
                   c.last_visit_time AS lastVisitTime,
                   c.create_time AS createTime,
                   TIMESTAMPDIFF(DAY, COALESCE(c.last_visit_time, c.create_time), NOW()) AS overdueDays
            FROM customer c
            WHERE c.iz_deleted = 0
              AND c.iz_valid = 1
              AND COALESCE(c.last_visit_time, c.create_time) < #{threshold}
            """)
    List<CustomerOverdueDTO> selectOverdue(@Param("threshold") LocalDateTime threshold);
}

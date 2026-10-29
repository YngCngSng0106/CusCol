package com.zlyd.customercollection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zlyd.customercollection.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}


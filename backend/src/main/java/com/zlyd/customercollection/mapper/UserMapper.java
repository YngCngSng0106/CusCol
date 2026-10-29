package com.zlyd.customercollection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zlyd.customercollection.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}


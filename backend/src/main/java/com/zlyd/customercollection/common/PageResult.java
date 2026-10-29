package com.zlyd.customercollection.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;

import java.util.List;

@Getter
public class PageResult<T> {

    private final List<T> records;
    private final long total;
    private final long pageNo;
    private final long pageSize;

    public PageResult(List<T> records, long total, long pageNo, long pageSize) {
        this.records = records;
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public static <T> PageResult<T> of(List<T> records, long total, long pageNo, long pageSize) {
        return new PageResult<>(records, total, pageNo, pageSize);
    }
}

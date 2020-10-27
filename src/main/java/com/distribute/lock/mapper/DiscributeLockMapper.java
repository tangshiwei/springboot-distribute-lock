package com.distribute.lock.mapper;

import com.distribute.lock.bean.DiscributeLock;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscributeLockMapper {

    @Select("select * from discribute_lock for update")
    List<DiscributeLock> findDiscributeLock();

}

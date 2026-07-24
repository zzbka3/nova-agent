package com.nova.agent.repository;

import com.nova.agent.model.po.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountMapper {

    Account selectById(@Param("id") Long id);

    List<Account> selectAll();

    int insert(Account record);

    int updateById(Account record);

    int deleteById(@Param("id") Long id);
}

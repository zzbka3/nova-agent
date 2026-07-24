package com.nova.agent.repository;

import com.nova.agent.model.po.Token;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TokenMapper {

    Token selectById(@Param("id") Long id);

    List<Token> selectAll();

    int insert(Token record);

    int updateById(Token record);

    int deleteById(@Param("id") Long id);
}

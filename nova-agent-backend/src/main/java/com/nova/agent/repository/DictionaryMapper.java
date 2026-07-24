package com.nova.agent.repository;

import com.nova.agent.model.po.Dictionary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictionaryMapper {

    Dictionary selectById(@Param("id") Long id);

    List<Dictionary> selectAll();

    int insert(Dictionary record);

    int updateById(Dictionary record);

    int deleteById(@Param("id") Long id);
}

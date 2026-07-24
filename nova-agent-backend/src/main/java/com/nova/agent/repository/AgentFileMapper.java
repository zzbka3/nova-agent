package com.nova.agent.repository;

import com.nova.agent.model.po.AgentFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentFileMapper {

    AgentFile selectById(@Param("id") Long id);

    List<AgentFile> selectAll();

    int insert(AgentFile record);

    int updateById(AgentFile record);

    int deleteById(@Param("id") Long id);
}

package com.nova.agent.react.repository;

import com.nova.agent.react.tool.ToolDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ToolDefinitionMapper {

    List<ToolDefinition> selectAll();

    List<ToolDefinition> selectByStatus(@Param("status") Integer status);

    ToolDefinition selectByToolId(@Param("toolId") String toolId);

    List<ToolDefinition> selectByToolIds(@Param("toolIds") List<String> toolIds);

    int insert(ToolDefinition record);

    int updateByToolId(ToolDefinition record);

    int deleteByToolId(@Param("toolId") String toolId);
}

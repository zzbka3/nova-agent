package com.nova.agent.repository;

import com.nova.agent.model.po.WorkFlowNodeDependency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkFlowNodeDependencyMapper {

    WorkFlowNodeDependency selectById(@Param("id") Long id);

    List<WorkFlowNodeDependency> selectAll();

    int insert(WorkFlowNodeDependency record);

    int updateById(WorkFlowNodeDependency record);

    int deleteById(@Param("id") Long id);
}

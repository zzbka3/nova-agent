package com.nova.agent.repository;

import com.nova.agent.model.po.AgentNodeExecuteLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentNodeExecuteLogMapper {

    int insert(AgentNodeExecuteLog record);

    int updateById(AgentNodeExecuteLog record);

    List<AgentNodeExecuteLog> selectByExecuteId(@Param("appId") String appId,
                                                  @Param("conversationId") String conversationId,
                                                  @Param("executeId") Long executeId);
}

package com.nova.agent.repository;

import com.nova.agent.model.po.AgentEdgeExecuteLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentEdgeExecuteLogMapper {

    int insert(AgentEdgeExecuteLog record);

    List<AgentEdgeExecuteLog> selectByExecuteId(@Param("appId") String appId,
                                                  @Param("conversationId") String conversationId,
                                                  @Param("executeId") Long executeId);
}

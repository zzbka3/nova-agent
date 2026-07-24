package com.nova.agent.repository;

import com.nova.agent.model.po.Agent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentMapper {

    Agent selectById(@Param("id") Long id);

    Agent selectByAppId(@Param("appId") String appId);

    List<Agent> selectAll();

    List<Agent> selectByQuery(@Param("query") String query,
                              @Param("isPublished") Integer isPublished,
                              @Param("agentType") Integer agentType);

    int insert(Agent record);

    int updateByAppId(Agent record);

    int updateStatus(@Param("appId") String appId, @Param("status") Integer status);

    int deleteByAppId(@Param("appId") String appId);

    Long selectLatestPublishedTime(@Param("appId") String appId);
}

package com.nova.agent.repository;

import com.nova.agent.model.po.AgentDraft;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AgentDraftMapper {

    AgentDraft selectByAppId(@Param("appId") String appId);

    int insert(AgentDraft record);

    int updateByAppId(AgentDraft record);

    int deleteByAppId(@Param("appId") String appId);

    int upsert(AgentDraft record);
}

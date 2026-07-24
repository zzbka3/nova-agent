package com.nova.agent.repository;

import com.nova.agent.model.po.AgentExecuteLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentExecuteLogMapper {

    int insert(AgentExecuteLog record);

    int updateResult(@Param("id") Long id, @Param("response") String response);

    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("response") String response);

    AgentExecuteLog selectById(@Param("id") Long id);

    List<AgentExecuteLog> selectByConversation(@Param("appId") String appId,
                                                @Param("conversationId") String conversationId,
                                                @Param("limit") Integer limit);

    AgentExecuteLog selectLastByConversation(@Param("appId") String appId,
                                              @Param("conversationId") String conversationId,
                                              @Param("timestamp") Long timestamp);

    Integer sumUsedTokens(@Param("appId") String appId,
                          @Param("conversationId") String conversationId,
                          @Param("executeId") Long executeId);
}

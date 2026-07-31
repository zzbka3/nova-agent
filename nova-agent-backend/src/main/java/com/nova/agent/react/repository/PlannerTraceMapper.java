package com.nova.agent.react.repository;

import com.nova.agent.react.trace.PlannerTrace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlannerTraceMapper {

    int insert(PlannerTrace record);

    List<PlannerTrace> selectByExecuteId(@Param("executeId") Long executeId);

    List<PlannerTrace> selectByConversation(@Param("appId") String appId,
                                             @Param("conversationId") String conversationId);

    List<PlannerTrace> selectByConversationAndLoop(@Param("appId") String appId,
                                                    @Param("conversationId") String conversationId,
                                                    @Param("executeId") Long executeId);
}

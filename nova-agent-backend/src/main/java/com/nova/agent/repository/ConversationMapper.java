package com.nova.agent.repository;

import com.nova.agent.model.po.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConversationMapper {

    int insert(Conversation record);

    Conversation selectByConversationId(@Param("appId") String appId,
                                         @Param("conversationId") String conversationId);
}

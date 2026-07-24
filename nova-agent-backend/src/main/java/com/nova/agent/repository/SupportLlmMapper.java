package com.nova.agent.repository;

import com.nova.agent.model.po.SupportLlm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SupportLlmMapper {

    List<SupportLlm> selectAll();

    List<SupportLlm> selectByModelType(@Param("modelType") String modelType);
}

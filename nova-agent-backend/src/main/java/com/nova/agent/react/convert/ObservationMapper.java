package com.nova.agent.react.convert;

import com.nova.agent.react.entity.ExecutionResult;
import com.nova.agent.react.entity.Observation;
import com.nova.agent.react.enums.ReActSignal;

/**
 * Converts ExecutionResult to Observation for Planner context.
 */
public class ObservationMapper {

    /**
     * Convert an ExecutionResult into an Observation entry.
     * Only successful results with CONTINUE signal are added as observations.
     * ERROR results are also recorded so Planner can see what went wrong.
     */
    public static Observation toObservation(ExecutionResult result) {
        Observation obs = new Observation();
        obs.setSuccess(result.isSuccess());
        obs.setSourceType(result.getSourceType());
        obs.setSourceId(result.getSourceId());
        obs.setSummary(result.getSummary());
        obs.setOutputs(result.getOutputs());

        // Map signal
        if (result.getSignal() != null) {
            obs.setSignal(result.getSignal().name());
        } else {
            obs.setSignal(result.isSuccess() ? ReActSignal.CONTINUE.name() : ReActSignal.ERROR.name());
        }

        obs.setMessage(result.getMessage());
        return obs;
    }
}

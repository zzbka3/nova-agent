package com.nova.agent.constant;

/**
 * Error code constants
 */
public class ErrorCode {

    public static final int SUCCESS = 0;
    public static final int GENERAL_ERROR = -1;
    public static final int NO_RUNNING_LOG = 1001;
    public static final int AGENT_NOT_EXIST = 1002;
    public static final int AGENT_VALIDATE_ERROR = 1003;
    public static final int AGENT_CYCLE_ERROR = 1004;
    public static final int AGENT_NOT_CONNECTED = 1005;
    public static final int AGENT_DEPENDENCY_ERROR = 1006;
    public static final int AGENT_EXECUTION_TIMEOUT = 1007;
    public static final int LLM_CALL_ERROR = 2001;
    public static final int MCP_CALL_ERROR = 2002;
    public static final int API_CALL_ERROR = 2003;
    public static final int CODE_EXECUTION_ERROR = 2004;
    public static final int KNOWLEDGE_RETRIEVAL_ERROR = 2005;
    public static final int AUTH_ERROR = 3001;
    public static final int QUOTA_EXCEEDED = 3002;
    public static final int RATE_LIMIT_ERROR = 3003;

    private ErrorCode() {}
}

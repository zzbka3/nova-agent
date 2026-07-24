package com.nova.agent.constant;

/**
 * Agent flow context variable name constants
 */
public class AgentFlowContextVar {

    public static final String USER_INVOKE_INPUT = "userInvokeInput";
    public static final String AGENT_FLOW_OUTPUT = "agentFlowOutput";
    public static final String KNOWLEDGE_DEFAULT_INPUT_VAR_NAME = "query";
    public static final String KNOWLEDGE_DEFAULT_OUTPUT_VAR_NAME = "OutputList";
    public static final String NODE_RESULT_PREFIX = "NODE_RESULT_";
    public static final String WORKFLOW_MESSAGE_PREFIX = "WORKFLOW_MESSAGE_PREFIX_";
    public static final String CURRENT_ACCOUNT = "currentAccount";
    public static final String MEMORY_VAR = "memoryVar";
    public static final String CURRENT_ACCOUNT_ID = "currentAccountId";
    public static final String WORKFLOW_AGENT_OUTPUT = "workflowAgentOutput";

    /** LLM reasoning content field name */
    public static final String LLM_REASONING_CONTENT_FIELD_NAME = "reasoning_content";
    public static final String INTENT_ID_FIELD_NAME = "classificationID";
    public static final String INTENT_NAME_FIELD_NAME = "classification";
    public static final String INTENT_THOUGHT_FIELD_NAME = "thought";

    public static final String EDGE_DEFAULT_CONDITION = "1==1";
    public static final String EDGE_DEFAULT_NOMATCH_CONDITION = "1!=1";
    public static final String DEFAULT_START_NODE_ID = "1";
    public static final String DEFAULT_START_NODE_ID_FILE_FIELD = "fileUrls";

    public static final String INTENT_PROMPT_TEMPLATE = """
            You need to perform the following tasks based on user input:
            
            Task 1: Determine the user's intent
            Task 2: Extract parameter values from the input based on the intent's parameter configuration
            
            **Important Requirements**:
            1. When extracting parameters, reasonable inference, calculation, and conversion are allowed and encouraged
            2. If inference results need to fill multiple parameters, provide all in extractVars
            3. Inference or conversion must ensure type consistency with varType
            
            Parameter types:
            - String, Integer, Boolean, Number, Object
            - ArrayString, ArrayInteger, ArrayBoolean, ArrayNumber, ArrayObject, ArrayAny
            
            Intent List:
            {{intent}}
            
            Output format (strictly JSON):
            {
                "intentId":"",
                "intentName":"",
                "extractVars":[
                    {
                        "varName":"",
                        "varType":"",
                        "varValue":"[]"
                    }
                ]
            }
            
            If no intent matches, set intentId = -1 and extractVars empty.
            Current date: {{nowDate}}.
            """;

    public static final String INTENT_PROMPT_SPEED_TEMPLATE = """
            Determine user intent as quickly as possible while maintaining accuracy.
            
            Intent List:
            {{intent}}
            
            Output format (strictly JSON):
            {
                "intentId":"",
                "intentName":""
            }
            
            If no intent matches, set intentId = -1.
            Current date: {{nowDate}}.
            """;

    private AgentFlowContextVar() {}
}

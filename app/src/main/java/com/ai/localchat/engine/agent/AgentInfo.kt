package com.ai.localchat.engine.agent

/** Agent 配置实体 */
data class AgentInfo(
    val agentName: String,
    val agentDesc: String,
    val promptTemplate: String,
    val isEnable: Boolean
)


package com.socialgateway.socialgateway.data.model

data class Prompt(
    val content: String,
    val answerable: Boolean
)

enum class PromptType(val value: String) { NORMAL("normal"), REFLECTION("reflection") }
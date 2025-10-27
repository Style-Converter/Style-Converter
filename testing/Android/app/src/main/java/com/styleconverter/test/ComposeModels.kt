package com.styleconverter.test

import kotlinx.serialization.Serializable

@Serializable
data class ComposeState(
    val name: String,
    val modifiers: List<String>
)

@Serializable
data class ComposeResponsive(
    val condition: String,
    val modifiers: List<String>
)

@Serializable
data class ComposeComponent(
    val name: String,
    val composableCode: String,
    val baseModifiers: List<String>,
    val states: List<ComposeState> = emptyList(),
    val responsive: List<ComposeResponsive> = emptyList()
)

@Serializable
data class ComposeDocument(
    val components: List<ComposeComponent>,
    val imports: List<String>
)

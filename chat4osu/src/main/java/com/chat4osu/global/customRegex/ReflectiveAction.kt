package com.chat4osu.global.customRegex

class ReflectiveAction(private val function: Any) : UniversalAction {
    private val method = function::class.java.methods.first { it.name == "invoke" }

    override fun invoke(params: List<Any?>) {
        try {
            val neededParams = method.parameterCount
            val actualParams = params.take(neededParams).toTypedArray()
            method.invoke(function, *actualParams)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to invoke action with params: $params", e)
        }
    }
}
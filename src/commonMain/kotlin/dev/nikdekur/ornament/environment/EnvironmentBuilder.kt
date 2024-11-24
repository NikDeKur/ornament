@file:OptIn(ExperimentalContracts::class)

package dev.nikdekur.ornament.environment

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * # EnvironmentBuilder
 *
 * Builder for the [Environment] interface.
 *
 * Allows creating an environment with specified values and requests.
 */
public class EnvironmentBuilder {

    /**
     * The function to get a value for a key.
     *
     * Recommended to use the [value] or [onGetValue] functions instead.
     */
    public var onGetValueFunc: (String) -> String? = { null }

    /**
     * The function to request a value for a key.
     *
     * Recommended to use the [request] or [onRequestValue] functions instead.
     */
    public var onRequestValueFunc: (String) -> String? = { null }

    public fun onGetValue(func: (String) -> String?) {
        onGetValueFunc = func
    }

    public fun onRequestValue(func: (String) -> String?) {
        onRequestValueFunc = func
    }


    /**
     * Set a value to be returned for the specified key.
     *
     * When the [Environment.getValue] method is called with the specified key,
     * the value will be returned.
     *
     * @param key the key
     * @param value the value
     */
    public fun value(key: String, value: String) {
        val previous = onGetValueFunc
        onGetValueFunc = { if (it == key) value else previous(it) }
    }

    /**
     * Request a value to be entered.
     *
     * When the [Environment.requestValue] method is called with the specified key,
     * the value will be returned.
     *
     * @param key the key
     * @param value the value
     */
    public fun request(key: String, value: String) {
        val previous = onRequestValueFunc
        onRequestValueFunc = { if (it == key) value else previous(it) }
    }

    /**
     * Build the environment.
     *
     * Creates an [Environment] instance with the specified values and requests.
     *
     * @return the environment
     */
    public fun build(): Environment {
        return object : Environment {
            override fun getValue(key: String) = onGetValueFunc(key)
            override fun requestValue(key: String, description: String) = onRequestValueFunc(key)
        }
    }
}

/**
 * Create an environment with the specified values and requests.
 *
 * DSL function for simplifying the creation of an environment.
 *
 * @param block the builder block
 * @return the environment
 */
public inline fun environment(block: EnvironmentBuilder.() -> Unit): Environment {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return EnvironmentBuilder().apply(block).build()
}
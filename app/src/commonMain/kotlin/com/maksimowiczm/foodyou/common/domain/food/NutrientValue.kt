package com.maksimowiczm.foodyou.common.domain.food

import kotlin.jvm.JvmInline

@kotlinx.serialization.Serializable(with = NutrientValueSerializer::class)
sealed interface NutrientValue {
    val value: Double?

    /** Represents a nutrient value that is known and complete. */
    @JvmInline
    @kotlinx.serialization.Serializable
    value class Complete(override val value: Double) : NutrientValue {
        operator fun plus(other: Complete) = Complete(value + other.value)

        override operator fun times(other: Double) = Complete(value * other)

        override operator fun div(other: Double) = Complete(value / other)
    }

    val isComplete: Boolean
        get() = this is Complete

    /**
     * Represents a nutrient value that is not known or incomplete (e.g. not all ingredients have
     * complete data).
     */
    @JvmInline
    @kotlinx.serialization.Serializable
    value class Incomplete(override val value: Double?) : NutrientValue {
        operator fun plus(other: Incomplete) =
            when {
                value == null && other.value == null -> Incomplete(null)
                else -> Incomplete((value ?: 0.0) + (other.value ?: 0.0))
            }
    }

    val isIncomplete: Boolean
        get() = this is Incomplete

    operator fun plus(other: NutrientValue): NutrientValue =
        when (this) {
            is Complete ->
                when (other) {
                    is Complete -> this + other
                    is Incomplete -> Incomplete(value + (other.value ?: 0.0))
                }

            is Incomplete ->
                when (other) {
                    is Complete -> Incomplete(other.value + (value ?: 0.0))
                    is Incomplete -> this + other
                }
        }

    operator fun times(other: Double): NutrientValue =
        when (this) {
            is Complete -> Complete(value * other)
            is Incomplete -> Incomplete(value?.times(other))
        }

    operator fun div(other: Double): NutrientValue =
        when (this) {
            is Complete -> Complete(value / other)
            is Incomplete -> Incomplete(value?.div(other))
        }

    companion object {
        fun from(value: Double) = Complete(value)

        fun from(value: Double?) =
            if (value == null) {
                Incomplete(value)
            } else {
                Complete(value)
            }

        fun Double?.toNutrientValue() = from(this)

        fun Float?.toNutrientValue() = from(this?.toDouble())
    }
}

/** Internal captures retain incomplete partial values as well as unknown versus explicit zero. */
object NutrientValueSerializer : kotlinx.serialization.KSerializer<NutrientValue> {
    @kotlinx.serialization.Serializable
    private data class StoredValue(val value: Double?, val complete: Boolean)
    override val descriptor = StoredValue.serializer().descriptor
    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: NutrientValue) =
        encoder.encodeSerializableValue(StoredValue.serializer(), StoredValue(value.value, value.isComplete))
    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): NutrientValue {
        val stored = decoder.decodeSerializableValue(StoredValue.serializer())
        return if (stored.complete) NutrientValue.Complete(requireNotNull(stored.value)) else NutrientValue.Incomplete(stored.value)
    }
}

fun List<NutrientValue>.sum(): NutrientValue =
    this.fold<NutrientValue, NutrientValue>(NutrientValue.Complete(0.0)) { acc, nutrientValue ->
        acc + nutrientValue
    }

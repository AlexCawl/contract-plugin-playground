package com.alexcawl.contract.playground.component.v1

import java.lang.Byte
import java.lang.Double
import java.lang.Float
import java.lang.Long
import java.lang.Short
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

abstract class ReflectiveEqualsHashCode {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val clazz = javaClass
        if (clazz != other?.javaClass) return false
        return getDeclaredFields(clazz).all { field ->
            val type = field.type
            if (type.isPrimitive) {
                when {
                    type === java.lang.Boolean.TYPE -> field.getBoolean(this) == field.getBoolean(other)
                    type === Character.TYPE -> field.getChar(this) == field.getChar(other)
                    type === Byte.TYPE -> field.getByte(this) == field.getByte(other)
                    type === Short.TYPE -> field.getShort(this) == field.getShort(other)
                    type === Integer.TYPE -> field.getInt(this) == field.getInt(other)
                    type === Long.TYPE -> field.getLong(this) == field.getLong(other)
                    type === Float.TYPE -> field.getFloat(this) == field.getFloat(other)
                    type === Double.TYPE -> field.getDouble(this) == field.getDouble(other)
                    else -> error("Invalid type")
                }
            } else {
                field.get(this)?.equals(field.get(other)) == true
            }
        }
    }

    override fun hashCode(): Int = getDeclaredFields(javaClass).fold(0) { acc, field ->
        31 * acc + (field.get(this)?.hashCode() ?: 0)
    }

    private fun getDeclaredFields(clazz: Class<*>): Array<out Field> =
        declaredFieldsCache.getOrPut(clazz) {
            clazz.declaredFields.onEach { it.isAccessible = true }
        }

    private companion object {
        private val declaredFieldsCache = ConcurrentHashMap<Class<*>, Array<out Field>>()
    }
}

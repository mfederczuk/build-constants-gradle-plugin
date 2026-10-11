/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package io.github.mfederczuk.buildconstants.datamodel.buildingdsl.prop

import io.github.mfederczuk.buildconstants.datamodel.TypedValue
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.PackageContentsBuilderDsl
import org.gradle.api.provider.Provider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@PackageContentsBuilderDsl
public class PropertyAssignersHolder(typedValueAssigner: PropertyAssigner<TypedValue>) {

	public val typedValue: PropertyAssigner<TypedValue> = typedValueAssigner

	public val booleanValue: PropertyAssigner<Boolean> by assigner(TypedValue::Boolean)

	public val byteValue: PropertyAssigner<Byte> by assigner(TypedValue::Byte)
	public val shortValue: PropertyAssigner<Short> by assigner(TypedValue::Short)
	public val intValue: PropertyAssigner<Int> by assigner(TypedValue::Int)
	public val longValue: PropertyAssigner<Long> by assigner(TypedValue::Long)

	public val uByteValue: PropertyAssigner<UByte> by assigner(TypedValue::UByte)
	public val uShortValue: PropertyAssigner<UShort> by assigner(TypedValue::UShort)
	public val uIntValue: PropertyAssigner<UInt> by assigner(TypedValue::UInt)
	public val uLongValue: PropertyAssigner<ULong> by assigner(TypedValue::ULong)

	public val floatValue: PropertyAssigner<Float> by assigner(TypedValue::Float)
	public val doubleValue: PropertyAssigner<Double> by assigner(TypedValue::Double)

	public val charValue: PropertyAssigner<Char> by assigner(TypedValue::Char)

	public val stringValue: PropertyAssigner<String> by assigner(TypedValue::String)
}

private fun <T : Any> assigner(
	createTypedValueOf: (T) -> TypedValue,
): ReadOnlyProperty<PropertyAssignersHolder, PropertyAssigner<T>> {
	return ReadOnlyProperty { thisRef: PropertyAssignersHolder, _: KProperty<*> ->
		MappedPropertyAssigner(
			downstream = thisRef.typedValue,
			transform = createTypedValueOf,
		)
	}
}

private class MappedPropertyAssigner<DownstreamValue : Any, in T : Any>(
	private val downstream: PropertyAssigner<DownstreamValue>,
	private val transform: (T) -> DownstreamValue,
) : PropertyAssigner<T> {

	override fun assign(value: T) {
		this.downstream.assign(this.transform(value))
	}

	override fun assign(valueProvider: Provider<out T>) {
		this.downstream.assign(valueProvider.map(this.transform))
	}

	override fun assign(provideValue: () -> T) {
		this.downstream.assign { this.transform(provideValue()) }
	}
}

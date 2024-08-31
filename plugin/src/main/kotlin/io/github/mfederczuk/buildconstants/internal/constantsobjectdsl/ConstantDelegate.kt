/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.internal.constantsobjectdsl

import io.github.mfederczuk.buildconstants.BuildConstantsExtension.ConstantsObject
import io.github.mfederczuk.buildconstants.ConstantValue
import io.github.mfederczuk.buildconstants.internal.convertTo
import org.gradle.api.provider.Property
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class ConstantDelegate<T : Any>(
	private val constantsObject: ConstantsObject,
	private val constantName: String,
	private val valueType: Class<T>,
) : ReadWriteProperty<Any?, T> {

	private val constantValueProperty: Property<ConstantValue>
		get() {
			val constant: ConstantsObject.Constant = this.constantsObject.constants.getByName(this.constantName)
			return constant.value
		}

	override fun getValue(thisRef: Any?, property: KProperty<*>): T {
		val constantValue: ConstantValue = this.constantValueProperty.get()
		return constantValue.untypedValue.convertTo(this.valueType)
	}

	override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		val constantValue: ConstantValue = ConstantValue.ofUntypedValue(value)
		this.constantValueProperty.set(constantValue)
	}
}

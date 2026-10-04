/*
 * SPDX-License-Identifier: CC0-1.0
 */

package io.github.mfederczuk.buildconstants.datamodel.buildingdsl.prop

import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.PackageContentsBuilderDsl

@PackageContentsBuilderDsl
public interface PropertyDefiner {

	/**
	 * Defines a new property with the name [name].
	 *
	 * The type and the value are set using the return value of this function:
	 *
	 * ```
	 * prop("VERSION_CODE").intValue = 69
	 * prop("TOKEN").stringValue = project.providers.gradleProperty("token")
	 *     .map(Any::toString)
	 * ```
	 */
	public fun prop(name: String): PropertyAssignersHolder
}

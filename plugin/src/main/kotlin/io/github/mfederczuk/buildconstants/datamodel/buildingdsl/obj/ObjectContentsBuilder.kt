/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj

import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.PackageContentsBuilder
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.PackageContentsBuilderDsl
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.prop.PropertyAssignersHolder
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.prop.PropertyDefiner

@PackageContentsBuilderDsl
public abstract class ObjectContentsBuilder : PropertyDefiner {

	/**
	 * Defines a new public property with the name [name] in this object.
	 *
	 * The type and the value are set using the return value of this function:
	 *
	 * ```
	 * prop("VERSION_CODE").intValue = 69
	 * prop("TOKEN").stringValue = project.providers.gradleProperty("token")
	 *     .map(Any::toString)
	 * ```
	 */
	abstract override fun prop(name: String): PropertyAssignersHolder

	// region deprecated stubs

	@Deprecated(
		message = "An object cannot contain a package",
		level = DeprecationLevel.ERROR,
	)
	public fun pkg(name: String): PackageContentsBuilder {
		throw NotImplementedError("Stub")
	}

	@Deprecated(
		message = "An object cannot contain a package",
		level = DeprecationLevel.ERROR,
	)
	public inline fun <R> pkg(name: String, action: PackageContentsBuilder.() -> R): R {
		throw NotImplementedError("Stub")
	}

	// endregion
}

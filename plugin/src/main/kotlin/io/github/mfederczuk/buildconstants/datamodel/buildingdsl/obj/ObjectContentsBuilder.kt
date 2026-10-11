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
public abstract class ObjectContentsBuilder : ObjectDefiner, PropertyDefiner {

	@PackageContentsBuilderDsl
	public interface InternalKeyword : ObjectDefiner, PropertyDefiner {

		/**
		 * Defines a new internal nested object with the name [name].
		 *
		 * The contents of the object are configured by [action].
		 *
		 * To define a nested *data* object, use `data.obj(name) { ... }`.
		 */
		override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit)

		/**
		 * Defines a new internal property with the name [name] in this object.
		 *
		 * The type and the value are set using the return value of this function:
		 *
		 * ```
		 * prop("VERSION_CODE").intValue = 69
		 * prop("TOKEN").stringValue = project.providers.gradleProperty("token")
		 *     .map(Any::toString)
		 * ```
		 */
		override fun prop(name: String): PropertyAssignersHolder
	}

	public abstract val internal: InternalKeyword

	/**
	 * Defines a new public nested object with the name [name].
	 *
	 * The contents of the object are configured by [action].
	 *
	 * To define an *internal* nested object, use `internal.obj(name) { ... }`, to define a *data* object, use
	 * `data.obj(name) { ... }`, and to define an *internal data* object, use `internal.data.obj(name) { ... }`.
	 */
	abstract override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit)

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
	 *
	 * To define an *internal* property, use `internal.prop(name)`.
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

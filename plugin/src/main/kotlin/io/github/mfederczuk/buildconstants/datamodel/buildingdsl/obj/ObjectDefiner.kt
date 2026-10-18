/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj

import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.PackageContentsBuilderDsl

/**
 * Interface that allows defining regular (non-data) or data objects.
 */
@PackageContentsBuilderDsl
public interface ObjectDefiner : UnmodifiedObjectDefiner {

	@PackageContentsBuilderDsl
	public interface DataKeyword : UnmodifiedObjectDefiner {

		/**
		 * Defines a new data object with the name [name].
		 *
		 * The contents of the object are configured by [action].
		 */
		override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit)
	}

	public val data: DataKeyword

	/**
	 * Defines a new object with the name [name].
	 *
	 * The contents of the object are configured by [action].
	 *
	 * To define a *data* object, use `data.obj(name) { ... }`.
	 */
	override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit)
}

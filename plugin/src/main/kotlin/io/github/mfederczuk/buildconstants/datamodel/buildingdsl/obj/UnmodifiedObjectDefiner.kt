/*
 * SPDX-License-Identifier: CC0-1.0
 */

package io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj

import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.PackageContentsBuilderDsl

/**
 * Interface that allows defining objects.
 */
@PackageContentsBuilderDsl
public interface UnmodifiedObjectDefiner {

	/**
	 * Defines a new object with the name [name].
	 *
	 * The contents of the object are configured by [action].
	 */
	public fun obj(name: String, action: ObjectContentsBuilder.() -> Unit)
}

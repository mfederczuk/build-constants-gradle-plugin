/*
 * SPDX-License-Identifier: CC0-1.0
 */

package io.github.mfederczuk.buildconstants.datamodel.buildingdsl.prop

import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.PackageContentsBuilderDsl
import org.gradle.api.SupportsKotlinAssignmentOverloading
import org.gradle.api.provider.Provider

@PackageContentsBuilderDsl
@SupportsKotlinAssignmentOverloading
public interface PropertyAssigner<in T : Any> {

	public fun assign(value: T)

	public fun assign(valueProvider: Provider<out T>)

	public fun assign(provideValue: () -> T)
}

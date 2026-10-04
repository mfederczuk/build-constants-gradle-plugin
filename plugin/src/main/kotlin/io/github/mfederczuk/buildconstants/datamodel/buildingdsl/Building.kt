/*
 * SPDX-License-Identifier: CC0-1.0
 */

package io.github.mfederczuk.buildconstants.datamodel.buildingdsl

import io.github.mfederczuk.buildconstants.datamodel.PackageContents
import io.github.mfederczuk.buildconstants.internal.datamodel.buildingdsl.PackageContentsBuilderImpl
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

public fun buildPackageContentsProvider(
	providerFactory: ProviderFactory,
	builderAction: PackageContentsBuilder.() -> Unit,
): Provider<PackageContents> {
	return PackageContentsBuilderImpl(providerFactory)
		.apply(builderAction)
		.closeAndBuildPackageContentsProvider()
}

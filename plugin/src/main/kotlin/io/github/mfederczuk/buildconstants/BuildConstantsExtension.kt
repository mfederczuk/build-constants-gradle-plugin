/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants

import io.github.mfederczuk.buildconstants.datamodel.PackageContents
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.PackageContentsBuilder
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.buildPackageContentsProvider
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

public abstract class BuildConstantsExtension {

	@get:Inject
	internal abstract val providerFactory: ProviderFactory

	/**
	 * It is *not* recommended to set this property manually and instead use the DSL via
	 * the functions [pkg()][pkg] or [rootPkg()][rootPkg].
	 */
	public abstract val rootPackageContents: Property<PackageContents>

	// region DSL

	/**
	 * Builds a new [PackageContents][PackageContents] [Provider][Provider] using [builderAction] and sets it to
	 * [rootPackageContents].
	 *
	 * Sub-packages and objects defined by [builderAction] are placed inside the root package.
	 *
	 * @see pkg
	 * @see buildPackageContentsProvider
	 */
	public fun rootPkg(builderAction: PackageContentsBuilder.() -> Unit) {
		val packageContentsProvider: Provider<PackageContents> =
			buildPackageContentsProvider(this.providerFactory, builderAction)

		this.rootPackageContents.set(packageContentsProvider)
	}

	/**
	 * Builds a new [PackageContents][PackageContents] [Provider][Provider] using [builderAction] and sets it to
	 * [rootPackageContents].
	 *
	 * Sub-packages and objects defined by [builderAction] are placed inside the package with the name [name].
	 *
	 * This is the recommended way to define objects and constants.
	 *
	 * @see rootPkg
	 */
	public inline fun pkg(name: String, crossinline builderAction: PackageContentsBuilder.() -> Unit) {
		return this.rootPkg { pkg(name).builderAction() }
	}

	// endregion
}

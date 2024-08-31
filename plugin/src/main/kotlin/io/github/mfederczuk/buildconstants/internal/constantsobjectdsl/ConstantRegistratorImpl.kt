/*
 * SPDX-License-Identifier: CC0-1.0
 */

package io.github.mfederczuk.buildconstants.internal.constantsobjectdsl

import io.github.mfederczuk.buildconstants.BuildConstantsExtension
import io.github.mfederczuk.buildconstants.ConstantDelegateProvider
import io.github.mfederczuk.buildconstants.ConstantRegistrator
import org.gradle.api.provider.ProviderFactory

internal class ConstantRegistratorImpl(
	private val providerFactory: ProviderFactory,
	private val constantsObject: BuildConstantsExtension.ConstantsObject,
) : ConstantRegistrator {

	override operator fun <T : Any> invoke(
		valueClass: Class<T>,
		valueSupplier: () -> Any,
	): ConstantDelegateProvider<T> {
		return ConstantDelegateProviderImpl(
			this.providerFactory,
			this.constantsObject,
			nameSpec = ConstantDelegateProviderImpl.NameSpec.Auto,
			valueClass,
			valueSupplier,
		)
	}

	override operator fun <T : Any> invoke(
		name: String,
		valueClass: Class<T>,
		valueSupplier: () -> Any,
	): ConstantDelegateProvider<T> {
		return ConstantDelegateProviderImpl(
			this.providerFactory,
			this.constantsObject,
			nameSpec = ConstantDelegateProviderImpl.NameSpec.Specific(name),
			valueClass,
			valueSupplier,
		)
	}
}

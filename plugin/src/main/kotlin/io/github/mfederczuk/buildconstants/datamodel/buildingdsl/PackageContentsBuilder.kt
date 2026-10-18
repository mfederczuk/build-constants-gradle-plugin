/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package io.github.mfederczuk.buildconstants.datamodel.buildingdsl

import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj.ObjectContentsBuilder
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj.ObjectDefiner
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@PackageContentsBuilderDsl
public abstract class PackageContentsBuilder : ObjectDefiner {

	@PackageContentsBuilderDsl
	public interface PublicKeyword : ObjectDefiner {

		/**
		 * Defines a new public object with the name [name] in this package.
		 *
		 * The contents of the object are configured by [action].
		 */
		override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit)
	}

	public abstract val public: PublicKeyword

	public abstract fun pkg(name: String): PackageContentsBuilder

	@OptIn(ExperimentalContracts::class)
	public inline fun <R> pkg(name: String, action: PackageContentsBuilder.() -> R): R {
		contract {
			callsInPlace(action, InvocationKind.EXACTLY_ONCE)
		}

		return this.pkg(name).action()
	}

	/**
	 * Defines a new internal object with the name [name] in this package.
	 *
	 * The contents of the object are configured by [action].
	 *
	 * To define a *public* object, use `public.obj(name) { ... }`, to define a *data* object, use
	 * `data.obj(name) { ... }`, and to define *public data* object, use `public.data.obj(name) { ... }`.
	 */
	abstract override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit)
}

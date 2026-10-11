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
	public inner class PublicKeyword : ObjectDefiner {

		override val data: ObjectDefiner.DataKeyword =
			object : ObjectDefiner.DataKeyword {

				override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
					this@PackageContentsBuilder.file(name = "$name.kt") { public.data.obj(name, action) }
				}
			}

		/**
		 * Defines a new public object with the name [name] in this package.
		 *
		 * The contents of the object are configured by [action].
		 */
		override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
			this@PackageContentsBuilder.file(name = "$name.kt") { public.obj(name, action) }
		}
	}

	public val public: PublicKeyword = this.PublicKeyword()

	override val data: ObjectDefiner.DataKeyword =
		object : ObjectDefiner.DataKeyword {

			override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
				this@PackageContentsBuilder.file(name = "$name.kt") { data.obj(name, action) }
			}
		}

	public abstract fun pkg(name: String): PackageContentsBuilder

	@OptIn(ExperimentalContracts::class)
	public inline fun <R> pkg(name: String, action: PackageContentsBuilder.() -> R): R {
		contract {
			callsInPlace(action, InvocationKind.EXACTLY_ONCE)
		}

		return this.pkg(name).action()
	}

	/**
	 * Defines a new file with the name [name] in this package.
	 *
	 * The contents of the file are configured by [action].
	 *
	 * If the only declaration in the file is an object, then the function [obj()][obj] can be used as a shortcut.
	 */
	public abstract fun file(name: String, action: FileContentsBuilder.() -> Unit)

	/**
	 * Defines a new internal object with the name [name] in this package.
	 *
	 * The contents of the object are configured by [action].
	 *
	 * To define a *public* object, use `public.obj(name) { ... }`, to define a *data* object, use
	 * `data.obj(name) { ... }`, and to define *public data* object, use `public.data.obj(name) { ... }`.
	 *
	 * @see file
	 */
	final override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
		this.file(name = "$name.kt") { obj(name, action) }
	}
}

/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public interface BuildConstantsExtension {

	public interface ConstantsObject : Named {

		public interface Constant : Named {

			/**
			 * The name of this constant.
			 */
			override fun getName(): String

			/**
			 * The value of this constant.
			 */
			public val value: Property<ConstantValue>
		}

		/**
		 * The ID name of the object.
		 *
		 * This is *not* the final name of the generated Kotlin object. (see [packageName] and [objectName])
		 */
		override fun getName(): String

		/**
		 * The name of the package to generate the Kotlin object in.
		 *
		 * @see objectName
		 */
		public val packageName: Property<String>

		/**
		 * The name of the generated Kotlin object.
		 *
		 * @see packageName
		 */
		public val objectName: Property<String>

		@get:Nested
		public val constants: NamedDomainObjectContainer<Constant>

		public fun constants(action: Action<in NamedDomainObjectContainer<Constant>>) {
			action.execute(this.constants)
		}
	}

	@get:Nested
	public val objects: NamedDomainObjectContainer<ConstantsObject>

	public fun objects(action: Action<in NamedDomainObjectContainer<ConstantsObject>>) {
		action.execute(this.objects)
	}
}

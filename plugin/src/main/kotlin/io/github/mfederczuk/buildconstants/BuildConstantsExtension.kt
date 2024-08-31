/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants

import io.github.mfederczuk.buildconstants.internal.PackageName
import io.github.mfederczuk.buildconstants.internal.constantsobjectdsl.ConstantRegistratorImpl
import io.github.mfederczuk.buildconstants.internal.quoted
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

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
		 * The visibility of the generated Kotlin object.
		 * The default is `internal`.
		 */
		public val visibility: Property<Visibility>

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

		public val Project.constant: ConstantRegistrator
			get() {
				return ConstantRegistratorImpl(
					providerFactory = this@constant.providers,
					constantsObject = this@ConstantsObject,
				)
			}
	}

	@get:Nested
	public val objects: NamedDomainObjectContainer<ConstantsObject>

	public fun objects(action: Action<in NamedDomainObjectContainer<ConstantsObject>>) {
		action.execute(this.objects)
	}

	// region DSL

	public operator fun String.invoke(id: String, block: ConstantsObject.() -> Unit) {
		val (packageName: PackageName, objectName: String) = this@invoke.splitPackageNameAndObjectName()
		this@BuildConstantsExtension.registerObject(id, packageName, objectName, block)
	}

	public operator fun String.invoke(block: ConstantsObject.() -> Unit) {
		val (packageName: PackageName, objectName: String) = this@invoke.splitPackageNameAndObjectName()

		val subPackagesReversed: List<String> = packageName.splitSubPackageNames()
			.toList()
			.asReversed()

		var id = ""

		for (component: String in (listOf(objectName) + subPackagesReversed)) {
			id = (component + id.uppercaseFirstChar())

			if (id in this@BuildConstantsExtension.objects.names) {
				continue
			}

			this@BuildConstantsExtension.registerObject(id, packageName, objectName, block)
			break
		}
	}

	private fun registerObject(
		id: String,
		packageName: PackageName,
		objectName: String,
		block: ConstantsObject.() -> Unit,
	) {
		this@BuildConstantsExtension.objects.register(id) {
			this@register.packageName.set(packageName.toString())
			this@register.objectName.set(objectName)

			with(this@register, block)
		}
	}

	// endregion
}

private fun String.splitPackageNameAndObjectName(): Pair<PackageName, String> {
	val components: List<String> = this.split('.')

	val packageNameStr: String = components.dropLast(1)
		.joinToString(separator = ".")
	val packageName: PackageName? = PackageName.ofString(packageNameStr)
	checkNotNull(packageName) {
		"Invalid package name: ${packageNameStr.quoted()}"
	}

	val objectName: String = components.last()

	return (packageName to objectName)
}

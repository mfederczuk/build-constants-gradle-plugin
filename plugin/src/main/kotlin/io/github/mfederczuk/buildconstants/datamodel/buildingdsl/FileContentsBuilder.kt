package io.github.mfederczuk.buildconstants.datamodel.buildingdsl

import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj.ObjectContentsBuilder
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj.ObjectDefiner
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.prop.PropertyAssignersHolder
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.prop.PropertyDefiner

@PackageContentsBuilderDsl
public abstract class FileContentsBuilder : ObjectDefiner, PropertyDefiner {

	@PackageContentsBuilderDsl
	public interface PublicKeyword : ObjectDefiner, PropertyDefiner {

		/**
		 * Defines a new public object with the name [name] in this file.
		 *
		 * The contents of the object are configured by [action].
		 *
		 * To define a *data* object, use `data.obj(name) { ... }`.
		 */
		override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit)

		/**
		 * Defines a new public property with the name [name] in this file.
		 *
		 * The type and the value are set using the return value of this function:
		 *
		 * ```
		 * prop("VERSION_CODE").intValue = 69
		 * prop("TOKEN").stringValue = project.providers.gradleProperty("token")
		 *     .map(Any::toString)
		 * ```
		 */
		override fun prop(name: String): PropertyAssignersHolder
	}

	public abstract val public: PublicKeyword

	/**
	 * Defines a new internal object with the name [name] in this file.
	 *
	 * The contents of the object are configured by [action].
	 *
	 * To define an *public* object, use `public.obj(name) { ... }`, to define a *data* object, use
	 * `data.obj(name) { ... }`, and to define an *public data* object, use `public.data.obj(name) { ... }`.
	 */
	abstract override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit)

	/**
	 * Defines a new internal property with the name [name] in this file.
	 *
	 * The type and the value are set using the return value of this function:
	 *
	 * ```
	 * prop("VERSION_CODE").intValue = 69
	 * prop("TOKEN").stringValue = project.providers.gradleProperty("token")
	 *     .map(Any::toString)
	 * ```
	 *
	 * To define an *public* property, use `public.prop(name)`.
	 */
	abstract override fun prop(name: String): PropertyAssignersHolder

	// region deprecated stubs

	@Deprecated(
		message = "A file cannot contain a package",
		level = DeprecationLevel.ERROR,
	)
	public fun pkg(name: String): PackageContentsBuilder {
		throw NotImplementedError("Stub")
	}

	@Deprecated(
		message = "A file cannot contain a package",
		level = DeprecationLevel.ERROR,
	)
	public inline fun <R> pkg(name: String, action: PackageContentsBuilder.() -> R): R {
		throw NotImplementedError("Stub")
	}

	// endregion
}

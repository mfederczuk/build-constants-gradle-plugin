/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.U_BYTE
import com.squareup.kotlinpoet.U_INT
import com.squareup.kotlinpoet.U_LONG
import com.squareup.kotlinpoet.U_SHORT
import io.github.mfederczuk.buildconstants.datamodel.KotlinIdentifier
import io.github.mfederczuk.buildconstants.datamodel.ObjectDetails
import io.github.mfederczuk.buildconstants.datamodel.PackageContents
import io.github.mfederczuk.buildconstants.datamodel.PropertyDetails
import io.github.mfederczuk.buildconstants.datamodel.TypedValue
import io.github.mfederczuk.buildconstants.datamodel.Visibility
import io.github.mfederczuk.buildconstants.internal.KotlinPackageName
import io.github.mfederczuk.buildconstants.internal.KotlinPackageName.Companion.plus
import io.github.mfederczuk.buildconstants.internal.KotlinPackageName.Companion.toPackageName
import kotlinx.collections.immutable.ImmutableMap
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

private data class Object(
	val packageName: KotlinPackageName?,
	val name: KotlinIdentifier,
	val details: ObjectDetails,
)

@CacheableTask
public abstract class BuildConstantsCodeGeneratingTask : DefaultTask() {

	@get:Input
	@get:Optional
	public abstract val rootPackageContents: Property<PackageContents>

	@get:OutputDirectory
	public abstract val outputDirectory: DirectoryProperty

	init {
		this.group = LifecycleBasePlugin.BUILD_GROUP
		this.description = "Generates the build constants Kotlin code files"

		this.onlyIf { task: Task ->
			require(task is BuildConstantsCodeGeneratingTask)

			task.rootPackageContents.getOrNull() != null
		}
	}

	@TaskAction
	internal fun generateCode() {
		val rootPackageContents: PackageContents? = this.rootPackageContents.getOrNull()
		if (rootPackageContents == null) {
			this.didWork = false
			return
		}

		val outputDirectoryPath: Path = this.outputDirectory.locationOnly.get().asFile.toPath()

		generateFiles(rootPackageContents, outputDirectoryPath)
		this.didWork = true
	}
}

private fun generateFiles(rootPackageContents: PackageContents, outputDirectoryPath: Path) {
	@OptIn(ExperimentalPathApi::class)
	outputDirectoryPath.deleteRecursively()

	for (`object`: Object in rootPackageContents.collectObjectsRecursively()) {
		val fileSpec: FileSpec =
			createObjectFileSpec(
				packageName = `object`.packageName,
				objectVisibility = `object`.details.visibility,
				objectName = `object`.name,
				objectProperties = `object`.details.properties,
			)

		fileSpec.writeTo(directory = outputDirectoryPath)
	}
}

private fun PackageContents.collectObjectsRecursively(): Sequence<Object> {
	return sequence {
		val rootPackageObjects: List<Object> = this@collectObjectsRecursively.objects
			.map { (objectName: KotlinIdentifier, objectDetails: ObjectDetails) ->
				Object(
					packageName = KotlinPackageName.Root,
					name = objectName,
					details = objectDetails,
				)
			}
		yieldAll(rootPackageObjects)

		this@collectObjectsRecursively.subPackages
			.forEach { (subPackageName: KotlinIdentifier, subPackagesContents: PackageContents) ->
				val subPackageObjects: Sequence<Object> = subPackagesContents.collectObjectsRecursively()
					.map { `object`: Object ->
						val packageName: KotlinPackageName =
							if (`object`.packageName != null) {
								subPackageName + `object`.packageName
							} else {
								subPackageName.toPackageName()
							}

						`object`.copy(packageName = packageName)
					}

				yieldAll(subPackageObjects)
			}
	}
}

private fun createObjectFileSpec(
	packageName: KotlinPackageName?,
	objectVisibility: Visibility,
	objectName: KotlinIdentifier,
	objectProperties: ImmutableMap<KotlinIdentifier, PropertyDetails>,
): FileSpec {
	val suppressAnnotationSpec: AnnotationSpec = AnnotationSpec.builder(Suppress::class)
		.useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
		.addMember("%S", "RedundantVisibilityModifier")
		.addMember("%S", "unused")
		.addMember("%S", "ConstPropertyName")
		.build()

	return FileSpec
		.get(
			packageName = packageName?.toString().orEmpty(),
			typeSpec = createObjectTypeSpec(objectVisibility, objectName, objectProperties),
		)
		.toBuilder()
		.addFileComment("This file was automatically generated. DO NOT EDIT!")
		.addAnnotation(suppressAnnotationSpec)
		.build()
}

private fun createObjectTypeSpec(
	visibility: Visibility,
	name: KotlinIdentifier,
	properties: ImmutableMap<KotlinIdentifier, PropertyDetails>,
): TypeSpec {
	val visibilityModifier: KModifier =
		when (visibility) {
			Visibility.Public -> KModifier.PUBLIC
			Visibility.Internal -> KModifier.INTERNAL
		}

	var builder: TypeSpec.Builder = TypeSpec.objectBuilder(name.toRawIdentifierString())
		.addModifiers(visibilityModifier)

	for ((propertyName: KotlinIdentifier, propertyDetails: PropertyDetails) in properties) {
		val propertySpec: PropertySpec =
			createPropertySpec(
				name = propertyName,
				typedValue = propertyDetails.typedValue,
			)

		builder = builder.addProperty(propertySpec)
	}

	return builder.build()
}

private fun createPropertySpec(name: KotlinIdentifier, typedValue: TypedValue): PropertySpec {
	return PropertySpec
		.builder(
			name.toRawIdentifierString(),
			typedValue.toClassName(),
			KModifier.PUBLIC,
			KModifier.CONST,
		)
		.initializer(typedValue.toInitializer())
		.build()
}

private fun TypedValue.toClassName(): ClassName {
	return when (this) {
		is TypedValue.Boolean -> BOOLEAN

		is TypedValue.Byte -> BYTE
		is TypedValue.Short -> SHORT
		is TypedValue.Int -> INT
		is TypedValue.Long -> LONG

		is TypedValue.UByte -> U_BYTE
		is TypedValue.UShort -> U_SHORT
		is TypedValue.UInt -> U_INT
		is TypedValue.ULong -> U_LONG

		is TypedValue.Float -> FLOAT
		is TypedValue.Double -> DOUBLE

		is TypedValue.Char -> CHAR

		is TypedValue.String -> STRING
	}
}

private fun TypedValue.toInitializer(): CodeBlock {
	val builder: CodeBlock.Builder = CodeBlock.builder()

	return when (this) {
		is TypedValue.Boolean -> builder.add("%L", this.value)

		is TypedValue.Byte -> builder.add("%L", this.value)
		is TypedValue.Short -> builder.add("%L", this.value)
		is TypedValue.Int -> builder.add("%L", this.value)
		is TypedValue.Long -> builder.add("%L", this.value)

		is TypedValue.UByte -> builder.add("%Lu", this.value)
		is TypedValue.UShort -> builder.add("%Lu", this.value)
		is TypedValue.UInt -> builder.add("%Lu", this.value)
		is TypedValue.ULong -> builder.add("%Lu", this.value)

		is TypedValue.Float -> builder.add("%L", this.value)
		is TypedValue.Double -> builder.add("%L", this.value)

		is TypedValue.Char -> {
			// KotlinPoet has no support for chars.
			when (this.value) {
				'\\' -> builder.add("'\\\\'")

				'\'' -> builder.add("'\\''")

				'\t' -> builder.add("'\\t'")
				'\b' -> builder.add("'\\b'")
				'\n' -> builder.add("'\\n'")
				'\r' -> builder.add("'\\r'")

				in (' '..'~') -> builder.add("'%L'", this.value)

				else -> {
					val escCode: String = this.value.code.toString(radix = 16)
						.uppercase()
						.padStart(4, '0')

					builder.add("'\\u%L'", escCode)
				}
			}
		}

		is TypedValue.String -> builder.add("%S", this.value)
	}.build()
}

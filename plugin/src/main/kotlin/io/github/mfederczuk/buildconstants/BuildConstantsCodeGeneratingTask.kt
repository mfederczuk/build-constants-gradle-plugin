/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: GPL-3.0-or-later
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
import io.github.mfederczuk.buildconstants.datamodel.FileContents
import io.github.mfederczuk.buildconstants.datamodel.FileEntryDetails
import io.github.mfederczuk.buildconstants.datamodel.KotlinFileName
import io.github.mfederczuk.buildconstants.datamodel.KotlinIdentifier
import io.github.mfederczuk.buildconstants.datamodel.ObjectDetails
import io.github.mfederczuk.buildconstants.datamodel.ObjectEntryDetails
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

private data class File(
	val packageName: KotlinPackageName?,
	val name: KotlinFileName,
	val contents: FileContents,
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

	for (file: File in rootPackageContents.collectFilesRecursively()) {
		val fileSpec: FileSpec =
			createFileSpec(
				packageName = file.packageName,
				name = file.name,
				contents = file.contents,
			)

		fileSpec.writeTo(directory = outputDirectoryPath)
	}
}

private fun PackageContents.collectFilesRecursively(): Sequence<File> {
	return sequence {
		val rootPackageFiles: List<File> = this@collectFilesRecursively.files
			.map { (fileName: KotlinFileName, fileContents: FileContents) ->
				File(
					packageName = KotlinPackageName.Root,
					name = fileName,
					contents = fileContents,
				)
			}
		yieldAll(rootPackageFiles)

		this@collectFilesRecursively.subPackages
			.forEach { (subPackageName: KotlinIdentifier, subPackagesContents: PackageContents) ->
				val subPackageFiles: Sequence<File> = subPackagesContents.collectFilesRecursively()
					.map { file: File ->
						val packageName: KotlinPackageName =
							if (file.packageName != null) {
								subPackageName + file.packageName
							} else {
								subPackageName.toPackageName()
							}

						file.copy(packageName = packageName)
					}

				yieldAll(subPackageFiles)
			}
	}
}

private fun createFileSpec(packageName: KotlinPackageName?, name: KotlinFileName, contents: FileContents): FileSpec {
	val suppressAnnotationSpec: AnnotationSpec = AnnotationSpec.builder(Suppress::class)
		.useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
		.addMember("%S", "REDUNDANT_VISIBILITY_MODIFIER")
		.addMember("%S", "ktlint")
		.addMember("%S", "unused")
		.addMember("%S", "ConstPropertyName")
		.build()

	var builder: FileSpec.Builder = FileSpec
		.builder(
			packageName = packageName?.toString().orEmpty(),
			fileName = name.toString(),
		)
		.addFileComment("This file was automatically generated. DO NOT EDIT!")
		.addAnnotation(suppressAnnotationSpec)

	for ((entryName: KotlinIdentifier, entryDetails: FileEntryDetails) in contents.entries) {
		builder = when (entryDetails) {
			is ObjectDetails -> {
				val objectTypeSpec: TypeSpec =
					createObjectTypeSpec(
						visibility = entryDetails.visibility,
						isData = entryDetails.isData,
						name = entryName,
						entries = entryDetails.entries,
					)

				builder.addType(objectTypeSpec)
			}

			is PropertyDetails -> {
				val propertySpec: PropertySpec =
					createPropertySpec(
						visibility = entryDetails.visibility,
						name = entryName,
						typedValue = entryDetails.typedValue,
					)

				builder.addProperty(propertySpec)
			}
		}
	}

	return builder.build()
}

private fun createObjectTypeSpec(
	visibility: Visibility,
	isData: Boolean,
	name: KotlinIdentifier,
	entries: ImmutableMap<KotlinIdentifier, ObjectEntryDetails>,
): TypeSpec {
	val visibilityModifier: KModifier =
		when (visibility) {
			Visibility.Public -> KModifier.PUBLIC
			Visibility.Internal -> KModifier.INTERNAL
		}

	var builder: TypeSpec.Builder = TypeSpec.objectBuilder(name.toRawIdentifierString())
		.addModifiers(visibilityModifier)

	if (isData) {
		builder = builder.addModifiers(KModifier.DATA)
	}

	for ((entryName: KotlinIdentifier, entryDetails: ObjectEntryDetails) in entries) {
		builder = when (entryDetails) {
			is ObjectDetails -> {
				val nestedObjectTypeSpec: TypeSpec =
					createObjectTypeSpec(
						visibility = entryDetails.visibility,
						isData = entryDetails.isData,
						name = entryName,
						entries = entryDetails.entries,
					)

				builder.addType(nestedObjectTypeSpec)
			}

			is PropertyDetails -> {
				val propertySpec: PropertySpec =
					createPropertySpec(
						visibility = entryDetails.visibility,
						name = entryName,
						typedValue = entryDetails.typedValue,
					)

				builder.addProperty(propertySpec)
			}
		}
	}

	return builder.build()
}

private fun createPropertySpec(visibility: Visibility, name: KotlinIdentifier, typedValue: TypedValue): PropertySpec {
	val visibilityModifier: KModifier =
		when (visibility) {
			Visibility.Public -> KModifier.PUBLIC
			Visibility.Internal -> KModifier.INTERNAL
		}

	return PropertySpec
		.builder(
			name.toRawIdentifierString(),
			typedValue.toClassName(),
			visibilityModifier,
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

/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.internal

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
import io.github.mfederczuk.buildconstants.ConstantValue
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.name
import kotlin.io.path.writer

internal fun generateConstantsObject(
	packageName: PackageName,
	objectName: String,
	constants: Map<String, ConstantValue>,
	outputFilePath: Path,
) {
	val fileSpec: FileSpec = buildFileSpec(packageName, objectName, constants, outputFilePath)

	outputFilePath.parent?.createDirectories()
	outputFilePath.writer()
		.use(fileSpec::writeTo)
}

private fun buildFileSpec(
	packageName: PackageName,
	objectName: String,
	constants: Map<String, ConstantValue>,
	outputFilePath: Path,
): FileSpec {
	val suppressAnnotationSpec: AnnotationSpec = AnnotationSpec.builder(Suppress::class)
		.useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
		.addMember("%S", "RedundantVisibilityModifier")
		.addMember("%S", "unused")
		.addMember("%S", "ConstPropertyName")
		.build()

	val objectTypeSpec: TypeSpec = buildConstantsObjectTypeSpec(objectName, constants)

	return FileSpec.builder(packageName.toString(), outputFilePath.name)
		.addFileComment("Generated code; DO NOT EDIT!")
		.addAnnotation(suppressAnnotationSpec)
		.addType(objectTypeSpec)
		.build()
}

private fun buildConstantsObjectTypeSpec(
	objectName: String,
	constants: Map<String, ConstantValue>,
): TypeSpec {
	val propertySpecs: List<PropertySpec> = constants
		.map { (name: String, value: ConstantValue) ->
			PropertySpec.builder(name, value.toKotlinPoetClassName(), KModifier.CONST)
				.initializer(value.toInitializer())
				.build()
		}

	return TypeSpec.objectBuilder(objectName)
		.addProperties(propertySpecs)
		.build()
}

private fun ConstantValue.toKotlinPoetClassName(): ClassName {
	return when (this) {
		is ConstantValue.Boolean -> BOOLEAN

		is ConstantValue.Byte -> BYTE
		is ConstantValue.UByte -> U_BYTE

		is ConstantValue.Short -> SHORT
		is ConstantValue.UShort -> U_SHORT

		is ConstantValue.Int -> INT
		is ConstantValue.UInt -> U_INT

		is ConstantValue.Long -> LONG
		is ConstantValue.ULong -> U_LONG

		is ConstantValue.Float -> FLOAT
		is ConstantValue.Double -> DOUBLE

		is ConstantValue.Char -> CHAR

		is ConstantValue.String -> STRING
	}
}

private fun ConstantValue.toInitializer(): CodeBlock {
	var builder: CodeBlock.Builder = CodeBlock.builder()

	builder = when (this) {
		is ConstantValue.Boolean -> builder.add("%L", this.value)

		is ConstantValue.Byte -> builder.add("%L", this.value)
		is ConstantValue.UByte -> builder.add("%Lu", this.value)

		is ConstantValue.Short -> builder.add("%L", this.value)
		is ConstantValue.UShort -> builder.add("%Lu", this.value)

		is ConstantValue.Int -> builder.add("%L", this.value)
		is ConstantValue.UInt -> builder.add("%Lu", this.value)

		is ConstantValue.Long -> builder.add("%L", this.value)
		is ConstantValue.ULong -> builder.add("%Lu", this.value)

		is ConstantValue.Float -> builder.add("%L", this.value)
		is ConstantValue.Double -> builder.add("%L", this.value)

		is ConstantValue.Char -> {
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

		is ConstantValue.String -> builder.add("%S", this.value)
	}

	return builder.build()
}

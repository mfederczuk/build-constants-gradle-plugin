/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.internal

import io.github.mfederczuk.buildconstants.BuildConstantsExtension
import io.github.mfederczuk.buildconstants.ConstantValue
import io.github.mfederczuk.buildconstants.ConstantsObjectGenerationTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

internal object ConstantsObjectGenerationTasks {

	fun registerFromExtensionData(
		taskContainer: TaskContainer,
		constantsObject: BuildConstantsExtension.ConstantsObject,
		baseOutputDirectoryProvider: Provider<Directory>,
	): TaskProvider<ConstantsObjectGenerationTask> {
		return taskContainer.register<ConstantsObjectGenerationTask>(name = constantsObject.toTaskName()) {
			val packageNameProvider: Provider<PackageName> = constantsObject.makeTypedPackageNameProvider()

			val constantsProvider: Provider<Map<String, ConstantValue>> = constantsObject.constants
				.toConstantsMapProvider(providerFactory = this@register.project.providers)

			val outputFileProvider: Provider<RegularFile> = baseOutputDirectoryProvider
				.zip(packageNameProvider, constantsObject.objectName) {
						buildDirectory: Directory,
						packageName: PackageName,
						objectName: String,
					->

					packageName.splitSubPackageNames()
						.fold(buildDirectory, Directory::dir)
						.file("$objectName.kt")
				}

			this@register.packageName.set(packageNameProvider.map(PackageName::toString))
			this@register.visibility.set(constantsObject.visibility)
			this@register.objectName.set(constantsObject.objectName)
			this@register.constants.set(constantsProvider)
			this@register.outputFile.convention(outputFileProvider)
		}
	}
}

private fun BuildConstantsExtension.ConstantsObject.makeTypedPackageNameProvider(): Provider<PackageName> {
	return this.packageName
		.map { packageNameStr: String ->
			val packageName: PackageName? = PackageName.ofString(packageNameStr)

			checkNotNull(packageName) {
				"Invalid package name ${packageNameStr.quoted()}"
			}
		}
}

private fun BuildConstantsExtension.ConstantsObject.toTaskName(): String {
	val objectIdNameProcessed: String = this.name
		.let { idName: String ->
			if (!(idName.first().isLetter())) {
				return@let idName
			}

			idName.uppercaseFirstChar()
		}

	return "generateConstantsObject$objectIdNameProcessed"
}

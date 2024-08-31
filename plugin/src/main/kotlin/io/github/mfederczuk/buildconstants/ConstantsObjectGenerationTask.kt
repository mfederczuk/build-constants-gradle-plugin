/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants

import io.github.mfederczuk.buildconstants.internal.PackageName
import io.github.mfederczuk.buildconstants.internal.generateConstantsObject
import io.github.mfederczuk.buildconstants.internal.quoted
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.nio.file.Path

@CacheableTask
public abstract class ConstantsObjectGenerationTask : DefaultTask() {

	init {
		this.group = LifecycleBasePlugin.BUILD_GROUP
	}

	@get:Input
	public abstract val packageName: Property<String>

	@get:Input
	public abstract val visibility: Property<Visibility>

	@get:Input
	public abstract val objectName: Property<String>

	@get:Input
	public abstract val constants: MapProperty<String, ConstantValue>

	@get:OutputFile
	public abstract val outputFile: RegularFileProperty

	@TaskAction
	internal fun generateConstantsObject() {
		val packageNameString: String = this.packageName.get()
		val packageName: PackageName? = PackageName.ofString(packageNameString)
		checkNotNull(packageName) {
			"Invalid package name ${packageNameString.quoted()}"
		}

		val visibility: Visibility = this.visibility.get()
		val objectName: String = this.objectName.get()
		val constants: Map<String, ConstantValue> = this.constants.get()

		val outputFilePath: Path = this.outputFile.asFile.get().toPath()

		generateConstantsObject(packageName, visibility, objectName, constants, outputFilePath)
	}
}

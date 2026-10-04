/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel

public class BuildConstantsPlugin : Plugin<Project> {

	public companion object {

		public const val EXTENSION_NAME: String = "buildConstants"

		public const val CODE_GENERATING_TASK_NAME: String = "generateBuildConstantsCode"
	}

	override fun apply(project: Project) {
		val extension: BuildConstantsExtension = project.extensions
			.create<BuildConstantsExtension>(name = EXTENSION_NAME)

		val outputDirectoryProvider: Provider<Directory> = project.layout.buildDirectory
			.dir("generated")
			.map { directory: Directory ->
				directory.dir("build-constants")
			}

		project.addGeneratedSourceDirectory(outputDirectoryProvider)

		registerCodeGeneratingTask(
			taskContainer = project.tasks,
			extension = extension,
			outputDirectoryProvider = outputDirectoryProvider,
		)
	}
}

private fun Project.addGeneratedSourceDirectory(directoryProvider: Provider<Directory>) {
	this.plugins.withType<IdeaPlugin> {
		this@addGeneratedSourceDirectory.extensions.configure<IdeaModel> {
			this@configure.module {
				this@module.generatedSourceDirs.add(directoryProvider.get().asFile)
			}
		}
	}

	this.extensions.configure<SourceSetContainer>("sourceSets") {
		this@configure.named("main") {
			this@named.extensions.configure<SourceDirectorySet>("kotlin") nestedConfigure@{
				this@nestedConfigure.srcDir(directoryProvider)
			}
		}
	}
}

private fun registerCodeGeneratingTask(
	taskContainer: TaskContainer,
	extension: BuildConstantsExtension,
	outputDirectoryProvider: Provider<Directory>,
) {
	val codeGeneratingTaskProvider: TaskProvider<BuildConstantsCodeGeneratingTask> =
		taskContainer.register<BuildConstantsCodeGeneratingTask>(name = BuildConstantsPlugin.CODE_GENERATING_TASK_NAME) {
			this@register.rootPackageContents.set(extension.rootPackageContents)
			this@register.outputDirectory.set(outputDirectoryProvider)
		}

	val externalDependantTaskNames: Set<String> = hashSetOf("compileKotlin", "sourcesJar", "kotlinSourcesJar")
	taskContainer.configureEach {
		if (this@configureEach.name in externalDependantTaskNames) {
			this@configureEach.dependsOn(codeGeneratingTaskProvider)
		}
	}

	taskContainer.named<Delete>("clean") {
		this@named.delete(codeGeneratingTaskProvider)
	}
}

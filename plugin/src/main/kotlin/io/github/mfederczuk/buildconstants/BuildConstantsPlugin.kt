/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants

import io.github.mfederczuk.buildconstants.internal.ConstantsObjectGenerationTasks
import io.github.mfederczuk.buildconstants.internal.ConstantsObjectsGenerationTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
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
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel

public class BuildConstantsPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val extension: BuildConstantsExtension =
			project.extensions.create<BuildConstantsExtension>(name = "buildConstants")

		val baseOutputDirectoryProvider: Provider<Directory> = project.layout.buildDirectory
			.dir("generated")
			.dir("build-constants")

		project.plugins.withType<IdeaPlugin> {
			project.extensions.configure<IdeaModel> {
				this@configure.module {
					this@module.generatedSourceDirs.add(baseOutputDirectoryProvider.get().asFile)
				}
			}
		}

		project.extensions.configure<SourceSetContainer>("sourceSets") {
			this@configure.named("main") {
				this@named.extensions.configure<SourceDirectorySet>("kotlin") nestedConfigure@{
					this@nestedConfigure.srcDir(baseOutputDirectoryProvider)
				}
			}
		}

		extension.objects
			.all {
				this@BuildConstantsPlugin
					.registerConstantsObjectGenerationTask(
						project,
						constantsObject = this@all,
						baseOutputDirectoryProvider,
					)
			}

		this.registerConstantsObjectsGenerationTaskIn(project)
	}

	private fun registerConstantsObjectGenerationTask(
		project: Project,
		constantsObject: BuildConstantsExtension.ConstantsObject,
		baseOutputDirectoryProvider: Provider<Directory>,
	) {
		val constantsObjectGenerationTaskProvider: TaskProvider<ConstantsObjectGenerationTask> =
			ConstantsObjectGenerationTasks.registerFromExtensionData(
				taskContainer = project.tasks,
				constantsObject,
				baseOutputDirectoryProvider,
			)

		project.tasks.addCompileKotlinDependency(constantsObjectGenerationTaskProvider)

		project.tasks.named<Delete>("clean") {
			this@named.delete(constantsObjectGenerationTaskProvider)
		}
	}

	private fun registerConstantsObjectsGenerationTaskIn(project: Project) {
		val constantsObjectsGenerationTaskProvider: TaskProvider<Task> =
			ConstantsObjectsGenerationTask.registerIn(taskContainer = project.tasks)

		project.tasks.addCompileKotlinDependency(constantsObjectsGenerationTaskProvider)
	}
}

private fun TaskContainer.addCompileKotlinDependency(taskProvider: TaskProvider<out Task>) {
	this.named("compileKotlin") {
		this@named.dependsOn(taskProvider)
	}
}

private fun Provider<Directory>.dir(path: String): Provider<Directory> {
	return this
		.map { directory: Directory ->
			directory.dir(path)
		}
}

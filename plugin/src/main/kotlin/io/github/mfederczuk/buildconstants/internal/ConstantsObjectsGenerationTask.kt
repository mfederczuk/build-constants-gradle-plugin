/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.internal

import io.github.mfederczuk.buildconstants.ConstantsObjectGenerationTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin

internal object ConstantsObjectsGenerationTask {

	fun registerIn(taskContainer: TaskContainer): TaskProvider<Task> {
		return taskContainer.register("generateConstantsObjects") {
			this@register.group = LifecycleBasePlugin.BUILD_GROUP

			val constantsObjectGenerationTaskNames: Set<String> = taskContainer
				.withType<ConstantsObjectGenerationTask>()
				.names
			this@register.dependsOn(*(constantsObjectGenerationTaskNames.toTypedArray()))
		}
	}
}

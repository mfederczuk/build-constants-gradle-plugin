/*
 * SPDX-License-Identifier: CC0-1.0
 */

plugins {
	id("com.gradle.plugin-publish") version "2.2.1" apply false
}

tasks.wrapper {
	distributionType = Wrapper.DistributionType.ALL
}

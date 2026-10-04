/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.datamodel

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Objects

// Values stored in Gradle properties must be serializable.
// However, the `Map` type is not inherently serializable, so the writeObject() and readObject() functions must be
// implemented to convert the Maps to and from an implementation that *does* implement Serializable.
// The type `LinkedHashMap` was chosen for this because it retains the original iteration order.
//
// Gradle seems to never actually deserialize any classes, which means that readObject() may not have to be implemented,
// the classes properties could be `val`s instead of `var`s, and they could be declared in the primary constructor.
// However, this seems like an implementation detail which should not be relied on, which is why
// the serialization API is *fully* implemented.

public class PackageContents(
	subPackages: ImmutableMap<KotlinIdentifier, PackageContents>,
	objects: ImmutableMap<KotlinIdentifier, ObjectDetails>,
) : Serializable {

	init {
		validatePackageContents(subPackages, objects)
	}

	public var subPackages: ImmutableMap<KotlinIdentifier, PackageContents> = subPackages
		private set

	public var objects: ImmutableMap<KotlinIdentifier, ObjectDetails> = objects
		private set

	// region Serializable

	@Throws(IOException::class)
	private fun writeObject(out: ObjectOutputStream) {
		out.writeObject(LinkedHashMap(this.subPackages))
		out.writeObject(LinkedHashMap(this.objects))
	}

	@Throws(IOException::class, ClassNotFoundException::class)
	private fun readObject(`in`: ObjectInputStream) {
		@Suppress("UNCHECKED_CAST")
		this.subPackages = (`in`.readObject() as Map<KotlinIdentifier, PackageContents>).toImmutableMap()
		@Suppress("UNCHECKED_CAST")
		this.objects = (`in`.readObject() as Map<KotlinIdentifier, ObjectDetails>).toImmutableMap()

		validatePackageContents(this.subPackages, this.objects)
	}

	// endregion

	// region equals(), hashCode() and toString()

	override fun equals(other: Any?): Boolean {
		return (this === other) || ((other is PackageContents) && this.equals(other))
	}

	private fun equals(other: PackageContents): Boolean {
		return (this.subPackages == other.subPackages) &&
			(this.objects == other.objects)
	}

	override fun hashCode(): Int {
		return Objects.hash(
			this.subPackages,
			this.objects,
		)
	}

	override fun toString(): String {
		return "PackageContents(" +
			"subPackages = ${this.subPackages}, " +
			"objects = ${this.objects}" +
			")"
	}

	// endregion
}

private fun validatePackageContents(
	subPackages: ImmutableMap<KotlinIdentifier, PackageContents>,
	objects: ImmutableMap<KotlinIdentifier, ObjectDetails>,
) {
	require(subPackages.isNotEmpty() || objects.isNotEmpty()) {
		"A package must not be empty"
	}

	for (subPackageName: KotlinIdentifier in subPackages.keys) {
		require('.' !in subPackageName.toRawIdentifierString()) {
			"Sub-package name ($subPackageName) must not contain a dot"
		}
	}
}

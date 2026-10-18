/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: GPL-3.0-or-later
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

public sealed interface ObjectEntryDetails : Serializable

public class ObjectDetails(
	visibility: Visibility,
	isData: Boolean,
	entries: ImmutableMap<KotlinIdentifier, ObjectEntryDetails>,
) : ObjectEntryDetails, Serializable {

	public var visibility: Visibility = visibility
		private set

	public var isData: Boolean = isData
		private set

	public var entries: ImmutableMap<KotlinIdentifier, ObjectEntryDetails> = entries
		private set

	// region Serializable

	@Throws(IOException::class)
	private fun writeObject(out: ObjectOutputStream) {
		out.writeInt(this.visibility.ordinal)
		out.writeBoolean(this.isData)
		out.writeObject(LinkedHashMap(this.entries))
	}

	@Throws(IOException::class, ClassNotFoundException::class)
	private fun readObject(`in`: ObjectInputStream) {
		this.visibility = Visibility.entries[`in`.readInt()]
		this.isData = `in`.readBoolean()
		@Suppress("UNCHECKED_CAST")
		this.entries = (`in`.readObject() as Map<KotlinIdentifier, ObjectEntryDetails>).toImmutableMap()
	}

	// endregion

	// region equals(), hashCode() and toString()

	override fun equals(other: Any?): Boolean {
		return (this === other) || ((other is ObjectDetails) && this.equals(other))
	}

	private fun equals(other: ObjectDetails): Boolean {
		return (this.visibility == other.visibility) &&
			(this.isData == other.isData) &&
			(this.entries == other.entries)
	}

	override fun hashCode(): Int {
		return Objects.hash(
			this.visibility,
			this.isData,
			this.entries,
		)
	}

	override fun toString(): String {
		return "ObjectDetails(" +
			"visibility = ${this.visibility}, " +
			"isData = ${this.isData}, " +
			"entries = ${this.entries}" +
			")"
	}

	// endregion
}

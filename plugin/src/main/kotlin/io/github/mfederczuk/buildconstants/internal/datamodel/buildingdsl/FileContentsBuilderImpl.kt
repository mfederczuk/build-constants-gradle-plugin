package io.github.mfederczuk.buildconstants.internal.datamodel.buildingdsl

import io.github.mfederczuk.buildconstants.datamodel.FileContents
import io.github.mfederczuk.buildconstants.datamodel.KotlinFileName
import io.github.mfederczuk.buildconstants.datamodel.KotlinIdentifier
import io.github.mfederczuk.buildconstants.datamodel.ObjectDetails
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.FileContentsBuilder
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj.ObjectContentsBuilder
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj.ObjectDefiner
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.prop.PropertyAssignersHolder
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal class FileContentsBuilderImpl(providerFactory: ProviderFactory) : FileContentsBuilder() {

	private class OpenState(val providerFactory: ProviderFactory) {
		val subPackageContentsBuilders: MutableMap<KotlinIdentifier, PackageContentsBuilderImpl> = hashMapOf()
		val objects: MutableMap<KotlinIdentifier, Provider<ObjectDetails>> = mutableMapOf()
		val
	}

	private var state: OpenState? = OpenState(providerFactory)

	override val public: PublicKeyword
		get() = TODO("Not yet implemented")

	override val data: ObjectDefiner.DataKeyword
		get() = TODO("Not yet implemented")

	override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
		TODO("Not yet implemented")
	}

	override fun prop(name: String): PropertyAssignersHolder {
		TODO("Not yet implemented")
	}
}

package io.github.worldsmiths.gradle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * gdx-gltf JSON model types ({@code net.mgsx.gltf.data}) whose fields LibGDX {@code Json} reads via
 * reflection. TeaVM strips unstated fields when {@code reflectionEnabled=false}; listing concrete
 * classes in {@code preservedClasses} keeps glTF/GLB loads working on web without global reflection.
 *
 * <p>Source of truth: {@code net.mgsx:gltf:2.3.0} jar (gdx-gltf). Reconcile when the pinned version
 * changes.
 */
public final class GltfDataTeaVmPreservedClasses {

    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(
            "net.mgsx.gltf.data.GLTF",
            "net.mgsx.gltf.data.GLTFAsset",
            "net.mgsx.gltf.data.GLTFEntity",
            "net.mgsx.gltf.data.GLTFExtensions",
            "net.mgsx.gltf.data.GLTFExtras",
            "net.mgsx.gltf.data.GLTFObject",
            "net.mgsx.gltf.data.animation.GLTFAnimation",
            "net.mgsx.gltf.data.animation.GLTFAnimationChannel",
            "net.mgsx.gltf.data.animation.GLTFAnimationSampler",
            "net.mgsx.gltf.data.animation.GLTFAnimationTarget",
            "net.mgsx.gltf.data.camera.GLTFCamera",
            "net.mgsx.gltf.data.camera.GLTFOrthographic",
            "net.mgsx.gltf.data.camera.GLTFPerspective",
            "net.mgsx.gltf.data.data.GLTFAccessor",
            "net.mgsx.gltf.data.data.GLTFAccessorSparse",
            "net.mgsx.gltf.data.data.GLTFAccessorSparseIndices",
            "net.mgsx.gltf.data.data.GLTFAccessorSparseValues",
            "net.mgsx.gltf.data.data.GLTFBuffer",
            "net.mgsx.gltf.data.data.GLTFBufferView",
            "net.mgsx.gltf.data.extensions.KHRLightsPunctual",
            "net.mgsx.gltf.data.extensions.KHRMaterialsEmissiveStrength",
            "net.mgsx.gltf.data.extensions.KHRMaterialsIOR",
            "net.mgsx.gltf.data.extensions.KHRMaterialsIridescence",
            "net.mgsx.gltf.data.extensions.KHRMaterialsPBRSpecularGlossiness",
            "net.mgsx.gltf.data.extensions.KHRMaterialsSpecular",
            "net.mgsx.gltf.data.extensions.KHRMaterialsTransmission",
            "net.mgsx.gltf.data.extensions.KHRMaterialsUnlit",
            "net.mgsx.gltf.data.extensions.KHRMaterialsVolume",
            "net.mgsx.gltf.data.extensions.KHRTextureTransform",
            "net.mgsx.gltf.data.geometry.GLTFMesh",
            "net.mgsx.gltf.data.geometry.GLTFMorphTarget",
            "net.mgsx.gltf.data.geometry.GLTFPrimitive",
            "net.mgsx.gltf.data.material.GLTFMaterial",
            "net.mgsx.gltf.data.material.GLTFpbrMetallicRoughness",
            "net.mgsx.gltf.data.scene.GLTFNode",
            "net.mgsx.gltf.data.scene.GLTFScene",
            "net.mgsx.gltf.data.scene.GLTFSkin",
            "net.mgsx.gltf.data.texture.GLTFImage",
            "net.mgsx.gltf.data.texture.GLTFNormalTextureInfo",
            "net.mgsx.gltf.data.texture.GLTFOcclusionTextureInfo",
            "net.mgsx.gltf.data.texture.GLTFSampler",
            "net.mgsx.gltf.data.texture.GLTFTexture",
            "net.mgsx.gltf.data.texture.GLTFTextureInfo"));

    private GltfDataTeaVmPreservedClasses() {}
}
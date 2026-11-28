package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/**
 * Headless GDX mesh bind / material edge cases (HLD §08 sibling tint, C2 wrapper dispose). No
 * Application / GL context — injectable mesh registry; sibling tint uses per-instance material
 * copies (mirrors glTF {@code ModelInstance} material ownership without a live GLTF load).
 */
class GdxD3MeshMaterialsTest {

    @Test
    void defaultGdxMeshesWiresProductionMeshOpener() throws Exception {
        GdxMeshes meshes = new GdxMeshes();
        java.lang.reflect.Field openerField = GdxMeshes.class.getDeclaredField("opener");
        openerField.setAccessible(true);
        Object opener = openerField.get(meshes);
        assertEquals("ProductionMeshOpener", opener.getClass().getSimpleName());
    }

    @Test
    void disposingLoadedModelWrapperLeavesRegistryBindable() {
        final AtomicBoolean registryDisposed = new AtomicBoolean();
        Model sharedGdx = proceduralGdxModel();
        GdxMeshes.LoadedMesh loaded =
                new GdxMeshes.LoadedMesh() {
                    @Override
                    public Model gdxModel() {
                        return sharedGdx;
                    }

                    @Override
                    public void dispose() {
                        registryDisposed.set(true);
                    }
                };
        GdxMeshes meshes = new GdxMeshes(path -> null);
        String path = "meshes/hero.glb";
        meshes.putFromAsync(path, loaded);

        GdxD3 d3 = new GdxD3(meshes);
        io.github.worldsmiths.core.d3.Model first = d3.createModelFromLoaded(path);
        first.dispose();

        assertFalse(registryDisposed.get());
        assertTrue(meshes.isMeshLoadedForBind(path));
        io.github.worldsmiths.core.d3.Model second = d3.createModelFromLoaded(path);
        assertNotNull(second);
        assertNotSame(first, second);
    }

    @Test
    void setDiffuseOnOneSiblingInstanceDoesNotRetintOther() {
        Model sharedGdx = proceduralGdxModel();
        com.badlogic.gdx.graphics.g3d.ModelInstance tintedDelegate =
                new com.badlogic.gdx.graphics.g3d.ModelInstance(sharedGdx);
        com.badlogic.gdx.graphics.g3d.ModelInstance siblingDelegate =
                new com.badlogic.gdx.graphics.g3d.ModelInstance(sharedGdx);
        tintedDelegate.materials.add(new Material(ColorAttribute.createDiffuse(Color.WHITE)));
        siblingDelegate.materials.add(new Material(ColorAttribute.createDiffuse(Color.WHITE)));
        GdxModelInstance tinted = new GdxModelInstance(tintedDelegate);
        GdxModelInstance sibling = new GdxModelInstance(siblingDelegate);

        sibling.setDiffuse(0.1f, 0.2f, 0.9f, 1f);
        tinted.setDiffuse(0.9f, 0.1f, 0.1f, 1f);

        Color siblingDiffuse = diffuseColor(sibling);
        assertNotNull(siblingDiffuse);
        assertEquals(0.1f, siblingDiffuse.r, 1e-5f);
        assertEquals(0.2f, siblingDiffuse.g, 1e-5f);
        assertEquals(0.9f, siblingDiffuse.b, 1e-5f);
    }

    /** CPU-only stub — no {@code ModelBuilder} / GL context. */
    private static Model proceduralGdxModel() {
        return new Model();
    }

    private static Color diffuseColor(GdxModelInstance instance) {
        for (Material material : instance.delegate.materials) {
            ColorAttribute attribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
            if (attribute != null) {
                return attribute.color;
            }
        }
        return null;
    }
}
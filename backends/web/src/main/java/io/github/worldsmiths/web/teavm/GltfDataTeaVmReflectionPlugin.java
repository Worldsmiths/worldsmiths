package io.github.worldsmiths.web.teavm;

import com.github.xpenatan.gdx.teavm.backends.shared.config.plugin.TeaReflectionSupplier;
import org.teavm.vm.spi.TeaVMHost;
import org.teavm.vm.spi.TeaVMPlugin;

/**
 * Targeted gdx-gltf JSON reflection for MESH on web. gdx-teavm 1.6.2 only applies
 * {@code gdx.teavm.reflection.classes} when {@code reflectionEnabled=true}; this plugin registers the
 * same concrete {@code net.mgsx.gltf.data.*} types while Worldsmiths keeps global reflection off (ADR-0007).
 */
public final class GltfDataTeaVmReflectionPlugin implements TeaVMPlugin {

    @Override
    public void install(TeaVMHost host) {
        TeaReflectionSupplier.addReflectionClass(GltfDataTeaVmPreservedClasses.ALL);
        TeaReflectionSupplier.installReflectionDependencySupport(host);
    }
}
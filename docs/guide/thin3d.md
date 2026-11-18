# Thin3D

Minimal 3D author surface through `EngineServices.thin3d()`. Package: `io.github.worldsmiths.core.thin3d`. No LibGDX types in the contract.

Covers the D9b rollball set: perspective camera, ambient + directional lights, primitive sphere/box meshes, model instances, and batch `begin` / `render` / `end` / `dispose`. No `.g3dj` / AssetManager load in D9.

## Ownership / dispose

`Thin3D` from the bag is a **factory**, not a disposable resource. Authors own dispose of every `Model` and `ModelBatch` they create (`model.dispose()`, `batch.dispose()` at shutdown).

These types need **no** dispose: `Thin3D` itself, `PerspectiveCamera`, `Environment`, `ModelBuilder`, and `ModelInstance`.

## API

### Factory (`Thin3D`)

| Method | Meaning |
| --- | --- |
| `createPerspectiveCamera(fovY, width, height)` | Perspective camera (FOV in degrees) |
| `createEnvironment()` | Lighting environment |
| `createModelBuilder()` | In-code primitive mesh builder |
| `createModelBatch()` | GPU batch (`begin` → `render` → `end`) |
| `createInstance(model)` | Drawable placement of a model |

### Camera / lights / meshes / batch

| Type | Key methods |
| --- | --- |
| `PerspectiveCamera` | `setNear` / `setFar` / `setPosition` / `setUp` / `lookAt` / `update` |
| `Environment` | `setAmbientLight(r,g,b,a)` / `addDirectionalLight(r,g,b,dirX,dirY,dirZ)` |
| `ModelBuilder` | `createSphere(...)` / `createBox(...)` — RGBA diffuse; position+normal attrs included |
| `Model` | `dispose()` (author-owned) |
| `ModelInstance` | `setTranslation` / `setTranslationAndScaling` (full overwrite) |
| `ModelBatch` | `begin(camera)` / `render(instance, environment)` / `end()` / `dispose()` (author-owned) |

## Usage

```java
private EngineServices services;
private ModelBatch batch;
private Environment environment;
private PerspectiveCamera camera;
private Model ballModel;
private ModelInstance ballInstance;

public void create(EngineServices services) {
  this.services = services;
  Thin3D thin3d = services.thin3d();
  ModelBuilder builder = thin3d.createModelBuilder();
  ballModel = builder.createSphere(1f, 1f, 1f, 24, 24, 0.2f, 0.55f, 0.95f, 1f);
  ballInstance = thin3d.createInstance(ballModel);
  batch = thin3d.createModelBatch();
  environment = thin3d.createEnvironment();
  environment.setAmbientLight(0.45f, 0.45f, 0.45f, 1f);
  environment.addDirectionalLight(0.85f, 0.85f, 0.8f, -0.6f, -1f, -0.3f);
  camera = thin3d.createPerspectiveCamera(
      67f, services.graphics().getWidth(), services.graphics().getHeight());
  camera.setNear(0.1f);
  camera.setFar(100f);
}

public void render() {
  ballInstance.setTranslationAndScaling(x, y, z, 1f, 1f, 1f);
  camera.setPosition(eyeX, eyeY, eyeZ);
  camera.setUp(0f, 1f, 0f);
  camera.lookAt(lookX, lookY, lookZ);
  camera.update();
  batch.begin(camera);
  batch.render(ballInstance, environment);
  batch.end();
}

public void dispose() {
  batch.dispose();
  ballModel.dispose();
}
```

Hold the bag from `create(EngineServices)`. Pair every `begin` with `end`. Dispose author-created models and the batch at shutdown — not the bag's `Thin3D`.

## Tests

`FakeEngineServices.thin3d()` returns a recording `FakeThin3D`: inspect `commands()`, `batchDisposed()`, and `modelsDisposed()` without a GPU. Sample sync helpers may still skip `create()`; prefer FakeThin3D when asserting the create/render path.

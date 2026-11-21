# Thin3D

Minimal 3D author surface through `EngineServices.thin3d()`. Package: `io.github.worldsmiths.core.thin3d`. No LibGDX types in the contract.

Covers the D9b rollball set plus Thin3D P1: shared `Camera`, pose-only orthographic camera, Input-space pick-ray, diffuse and emissive sphere/box creates, and instance translation / euler / scale. No `.g3dj` / AssetManager load in D9.

## Ownership / dispose

`Thin3D` from the bag is a **factory**, not a disposable resource. Authors own dispose of every `Model` and `ModelBatch` they create (`model.dispose()`, `batch.dispose()` at shutdown).

These types need **no** dispose: `Thin3D` itself, `Camera` / `PerspectiveCamera` / `OrthographicCamera`, `Environment`, `ModelBuilder`, `ModelInstance`, `Ray`, and `Vec3`.

## API

### Factory (`Thin3D`)

| Method | Meaning |
| --- | --- |
| `createPerspectiveCamera(fovY, width, height)` | Perspective camera (FOV in degrees) |
| `createOrthographicCamera(viewportWidth, viewportHeight)` | Pose-only orthographic camera (world-unit viewport; no zoom / setToOrtho) |
| `createEnvironment()` | Lighting environment |
| `createModelBuilder()` | In-code primitive mesh builder |
| `createModelBatch()` | GPU batch (`begin` → `render` → `end`) |
| `createInstance(model)` | Drawable placement of a model |

### Camera / lights / meshes / batch

| Type | Key methods |
| --- | --- |
| `Camera` | Shared pose + `update` + `getPickRay(screenX, screenY)` → immutable `Ray` |
| `PerspectiveCamera` | Extends `Camera`; `setNear` / `setFar` / `setPosition` / `setUp` / `lookAt` / `update` / `getPickRay` |
| `OrthographicCamera` | Extends `Camera`; same pose / pick surface (pose-only) |
| `Environment` | `setAmbientLight(r,g,b,a)` / `addDirectionalLight(r,g,b,dirX,dirY,dirZ)` |
| `ModelBuilder` | `createSphere(...)` / `createBox(...)` — RGBA diffuse; `createSphereEmissive` / `createBoxEmissive` — diffuse RGBA + emissive RGBA; position+normal attrs included |
| `Model` | `dispose()` (author-owned) |
| `ModelInstance` | `setTranslation` / `setTranslationAndScaling` / `setTranslationRotationScaling` (full overwrite; euler degrees X then Y then Z) |
| `ModelBatch` | `begin(camera)` / `render(instance, environment)` / `end()` / `dispose()` (author-owned) |
| `Ray` / `Vec3` | Immutable pick math (`Ray.origin`, `Ray.direction`; public `x`/`y`/`z` floats) |

### Input Y and pick-ray

`Camera.getPickRay(screenX, screenY)` takes **Input-space** screen coords: origin at the **top-left**, **Y down** — the same convention as `Input.getX()` / `Input.getY()`. Pass those values directly; do not flip Y for Thin3D. Call `camera.update()` after pose changes before picking or `ModelBatch.begin`. Skipping `update` leaves pick / begin undefined.

Each `getPickRay` returns a **fresh** immutable `Ray` (never a reused buffer). The game owns board-plane intersection; Thin3D does not project to cells.

### Board-plane intersect (author-owned)

```java
Ray ray = camera.getPickRay(services.input().getX(), services.input().getY());
// Example: hit horizontal board plane y = 0. Guard direction.y ≈ 0 in real code.
float t = -ray.origin.y / ray.direction.y;
float hitX = ray.origin.x + t * ray.direction.x;
float hitZ = ray.origin.z + t * ray.direction.z;
// Map hitX/hitZ to a cell in game space.
```

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

### Orthographic + emissive + TRS (P1)

```java
OrthographicCamera camera = thin3d.createOrthographicCamera(12f, 12f);
camera.setPosition(0f, 10f, 0f);
camera.setUp(0f, 0f, -1f);
camera.lookAt(0f, 0f, 0f);
camera.setNear(0.1f);
camera.setFar(100f);
camera.update();

Model beam = thin3d.createModelBuilder()
    .createBoxEmissive(0.1f, 0.1f, 2f, 1f, 1f, 1f, 1f, 1f, 0.4f, 0.1f, 1f);
ModelInstance inst = thin3d.createInstance(beam);
inst.setTranslationRotationScaling(0f, 0.05f, 0f, 0f, 45f, 0f, 1f, 1f, 1f);

batch.begin(camera);
batch.render(inst, environment);
batch.end();

Ray ray = camera.getPickRay(services.input().getX(), services.input().getY());
```

Diffuse `createSphere` / `createBox` stay unchanged. Emissive creates take the same geometry + diffuse RGBA, then emissive `er, eg, eb, ea`.

## Tests

`FakeEngineServices.thin3d()` returns a recording `FakeThin3D`: inspect `commands()`, `batchDisposed()`, and `modelsDisposed()` without a GPU. Seed pick returns with `seedPickRay` before `getPickRay` (empty seed throws). Sample sync helpers may still skip `create()`; prefer FakeThin3D when asserting the create/render path.

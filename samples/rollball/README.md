# Rollball (D8)

Nested 3D sample: collect pickups on a pad with a follow camera. Multi-platform founder checklists land in T6.

## Desktop smoke (founder)

```bash
./gradlew -p samples/rollball :desktop:run
```

Checklist:

1. WASD / arrows move the ball on the pad.
2. Touching a gold pickup collects it; title score increments.
3. Collect all 8 → title shows `WIN`.
4. Drive off the pad (fall) or far out of the arena → title shows `FAIL`.
5. After WIN/FAIL, press `R` or `Space` to restart.
6. Close the window cleanly (dispose frees Models / ModelBatch).

CI compile/test ≠ play confirmation.

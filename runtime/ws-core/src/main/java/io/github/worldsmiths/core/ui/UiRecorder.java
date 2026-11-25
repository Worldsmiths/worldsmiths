package io.github.worldsmiths.core.ui;

/** Optional session recorder for {@link FakeUiKit}. */
interface UiRecorder {

  void onCreate(String id, UiLeafType type);

  void onLayout(String id, float x, float y, float w, float h);

  void onHit(String id, boolean activated);

  void onFocus(String id);

  void onActivate(String id);

  void onDispose(String id);

  void onDraw(UiLeafType type, String id);

  UiRecorder NONE =
      new UiRecorder() {
        @Override
        public void onCreate(String id, UiLeafType type) {}

        @Override
        public void onLayout(String id, float x, float y, float w, float h) {}

        @Override
        public void onHit(String id, boolean activated) {}

        @Override
        public void onFocus(String id) {}

        @Override
        public void onActivate(String id) {}

        @Override
        public void onDispose(String id) {}

        @Override
        public void onDraw(UiLeafType type, String id) {}
      };
}
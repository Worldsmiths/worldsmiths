package io.github.worldsmiths.spike.web;

import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

/** Writes the round-trip report into the page as text, where a headless browser can read it. */
final class BrowserPage {
    private BrowserPage() {
    }

    static void show(boolean passed, String report) {
        HTMLDocument document = HTMLDocument.current();
        HTMLElement pre = document.createElement("pre");
        pre.setAttribute("id", "ws-scene");
        pre.setAttribute("data-result", passed ? "pass" : "fail");
        pre.setAttribute("style", "position:absolute;top:0;left:0;margin:12px;padding:8px;font:13px monospace;"
                + "white-space:pre-wrap;max-width:95vw;background:#111;color:" + (passed ? "#8f8" : "#f88"));
        pre.setInnerText(report);
        document.getBody().appendChild(pre);
        document.setTitle("WS-S2 " + (passed ? "PASS" : "FAIL"));
    }
}

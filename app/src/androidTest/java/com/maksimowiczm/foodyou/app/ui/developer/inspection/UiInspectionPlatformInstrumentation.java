package com.maksimowiczm.foodyou.app.ui.developer.inspection;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Platform-only smoke runner for the actual optimized application. It deliberately has no
 * AndroidX, Kotlin, or application-class linkage, so its test dex cannot depend on R8-inlined
 * implementation classes. Run only against the isolated English synthetic emulator instance.
 * UI actions own every preference change; no diary, credential, relay, or database values are read.
 */
public final class UiInspectionPlatformInstrumentation extends Instrumentation {
    private static final String MAIN_ACTIVITY =
            "com.maksimowiczm.foodyou.app.infrastructure.android.MainActivity";
    private static final String SETTING = "Developer tools: UI name bar";
    private static final String BAR = "UI name bar";
    private static final String COPY = "Copy UI locator";
    private static final String SETTINGS_SOURCE =
            "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/SettingsScreen.kt";
    private static final String COMPONENT_SOURCE =
            "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/ArrowBackIconButton.kt";
    private static final String TRANSLATION_WARNING =
            "Translations are created by volunteers. They may not be perfect and some translations may be missing. "
                    + "If you notice any mistakes don't hesitate to help improving them.";

    private UiAutomation automation;
    private String targetPackage;
    private Activity activity;
    private boolean enabledByRunner;
    private boolean reportedSettingGeometry;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        start();
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle result = new Bundle();
        int originalFlags = 0;
        Throwable failure = null;
        try {
            require(Build.VERSION.SDK_INT >= 31, "Platform evidence runner requires API 31 or later");
            automation = getUiAutomation();
            originalFlags = automation.getServiceInfo().flags;
            AccessibilityServiceInfo info = automation.getServiceInfo();
            info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            automation.setServiceInfo(info);
            targetPackage = getTargetContext().getPackageName();
            require("io.github.jarrydadaens.foodus".equals(targetPackage),
                    "Minified runtime must target the shipping application identity");
            runAcceptance();
        } catch (Throwable error) {
            failure = error;
        } finally {
            if (enabledByRunner) {
                try {
                    disableThroughUi();
                } catch (Throwable cleanupError) {
                    if (failure == null) failure = cleanupError;
                    else failure.addSuppressed(cleanupError);
                }
            }
            if (automation != null) {
                AccessibilityServiceInfo info = automation.getServiceInfo();
                info.flags = originalFlags;
                automation.setServiceInfo(info);
            }
        }
        if (failure == null) {
            result.putBoolean("foodusInspectionPass", true);
            result.putStringArray("foodusInspectionEvidence", new String[]{
                    "/data/local/tmp/foodus-story29-minified-settings.xml",
                    "/data/local/tmp/foodus-story29-minified-modal.xml",
                    "/data/local/tmp/foodus-story29-minified-settings.png"});
            result.putString("stream", "PASS: actual minified UI inspection, clipboard, source hierarchy, modal retention, navigation, and disablement\n");
            finish(Activity.RESULT_OK, result);
        } else {
            result.putBoolean("foodusInspectionPass", false);
            StringWriter stack = new StringWriter();
            failure.printStackTrace(new PrintWriter(stack));
            result.putString("stream", "FAIL: actual minified UI inspection\n" + stack);
            finish(Activity.RESULT_CANCELED, result);
        }
    }

    private void runAcceptance() throws Exception {
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .setClassName(targetPackage, MAIN_ACTIVITY)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity = startActivitySync(intent);
        runOnMainSync(() -> activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
        checkpoint("launched-actual-minified-activity");
        completeStartup();
        click("Go to settings", false);
        find("Settings", false);
        AccessibilityNodeInfo settingSwitch = settingSwitch();
        require(!settingSwitch.isChecked(), "UI name bar must initially be disabled in the fresh target");
        require(findNow(BAR) == null, "Default-off must not render inspector chrome");
        click(SETTING, true);
        enabledByRunner = true;
        await("enabled inspector chrome", () -> findNow(BAR) != null);
        require(settingSwitch().isChecked(), "Actual Settings switch must be enabled");
        require(!hierarchyCheckbox().isChecked(), "Hierarchy preference must initially be off");
        checkpoint("enabled-through-actual-settings");

        deepHold(find("Go back", false));
        find("Settings", false);
        String shortCopy = copyLocator();
        equal("ArrowBackIconButton.Icon.1", shortCopy, "Short copy is the actual shared Back icon name");
        require(hierarchyCheckbox().performAction(AccessibilityNodeInfo.ACTION_CLICK),
                "Actual hierarchy checkbox must accept a click");
        await("checked hierarchy preference", () -> hierarchyCheckbox().isChecked());
        String settingsXml = copyLocator();
        contains(settingsXml, "<ui-selection format=\"foodus-ui-v1\"", "Versioned copied XML");
        contains(settingsXml, "name=\"SettingsScreen.ArrowBackIconButton.1\"", "Actual caller survives minification");
        contains(settingsXml, "name=\"ArrowBackIconButton.IconButton.1\"", "Shared button ancestry survives minification");
        contains(settingsXml, "name=\"ArrowBackIconButton.Icon.1\"", "Selected child survives minification");
        contains(settingsXml, "source=\"" + SETTINGS_SOURCE + "\"", "Actual Settings source locator");
        contains(settingsXml, "source=\"" + COMPONENT_SOURCE + "\"", "Actual shared component source locator");
        contains(settingsXml, "call-site-owner=\"com.maksimowiczm.foodyou.app.ui.settings.SettingsScreen\"", "Actual shared child caller");
        contains(settingsXml, "call-site-source=\"" + SETTINGS_SOURCE + "\"", "Actual child caller source");
        writeEvidence("settings.xml", settingsXml.getBytes(StandardCharsets.UTF_8));
        saveSettingsScreenshot();
        checkpoint("verified-actual-short-and-hierarchy-copy");

        // A consumed deep hold must leave navigation intact; a subsequent ordinary tap must work.
        shortTap(find("Go back", false));
        find("Go to settings", false);
        click("Go to settings", false);
        find("Settings", false);
        click("Personalization", true);
        find("Personalization", false);
        List<Integer> previousWindows = new ArrayList<>();
        for (AccessibilityNodeInfo root : applicationRoots()) previousWindows.add(root.getWindowId());
        click("Graphs", true);
        await("actual graph menu window", () -> findInNewWindow("Bar", previousWindows) != null);
        AccessibilityNodeInfo menuItem = findInNewWindow("Bar", previousWindows);
        int menuWindow = menuItem.getWindowId();
        deepHold(menuItem);
        require(hasApplicationWindow(menuWindow), "Inspecting the menu must not activate its option");
        require(automation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK), "Dismiss the actual graph menu");
        await("dismissed graph menu window", () -> !hasApplicationWindow(menuWindow));
        String modalXml = copyLocator();
        contains(modalXml, "name=\"GraphStyleSettingsListItem.Text.1\"", "Actual modal selected text");
        contains(modalXml, "composable=\"androidx.compose.material3.DropdownMenu\"", "Actual modal surface identity");
        contains(modalXml, "/app/ui/personalization/PersonalizationScreen.kt\"", "Actual modal source locator");
        writeEvidence("modal.xml", modalXml.getBytes(StandardCharsets.UTF_8));
        checkpoint("verified-actual-modal-snapshot-after-dismissal");
        click("Go back", false);
        find("Settings", false);
        disableThroughUi();
        checkpoint("disabled-through-actual-settings");
    }

    private void completeStartup() throws Exception {
        long deadline = SystemClock.uptimeMillis() + 60_000;
        boolean agreed = false;
        boolean skipped = false;
        while (SystemClock.uptimeMillis() < deadline) {
            if (findNow("Preview release") != null) {
                click(findNow("Don't show again") != null ? "Don't show again" : "OK", false);
            } else if (findNow(TRANSLATION_WARNING) != null) {
                click("OK", false);
            } else if (findNow("What's new") != null) {
                require(automation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK), "Dismiss known startup changelog");
            } else if (findNow("Before you start") != null && !agreed) {
                click("Agree & Continue", false);
                agreed = true;
            } else if (findNow("Food database") != null && !skipped) {
                // Skip keeps every source unchecked and performs no import or download.
                click("Skip", false);
                skipped = true;
            } else if (findNow("Go to settings") != null) {
                checkpoint("startup-ready-without-database-import");
                return;
            }
            SystemClock.sleep(150);
        }
        throw new AssertionError("Known English startup controls did not reach the actual application shell");
    }

    private void disableThroughUi() throws Exception {
        for (int step = 0; step < 4 && findNow("Settings") == null; step++) {
            if (findNow("Go to settings") != null) click("Go to settings", false);
            else {
                automation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                SystemClock.sleep(250);
            }
        }
        find("Settings", false);
        if (findNow(BAR) != null && hierarchyCheckbox().isChecked()) {
            require(hierarchyCheckbox().performAction(AccessibilityNodeInfo.ACTION_CLICK),
                    "Restore the actual hierarchy checkbox before disabling inspection");
            await("restored hierarchy preference", () -> !hierarchyCheckbox().isChecked());
        }
        if (settingSwitch().isChecked()) click(SETTING, true);
        await("disabled inspector chrome", () -> findNow(BAR) == null);
        require(!settingSwitch().isChecked(), "Actual Settings switch must be disabled");
        enabledByRunner = false;
    }

    private AccessibilityNodeInfo settingSwitch() throws Exception {
        AccessibilityNodeInfo label = find(SETTING, true);
        for (int attempt = 0; attempt < 3; attempt++) {
            AccessibilityNodeInfo row = clickableAncestor(label);
            if (row != null) row.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN.getId());
            waitForIdleSync();
            SystemClock.sleep(250);
            label = findNow(SETTING);
            if (label != null) {
                row = clickableAncestor(label);
                AccessibilityNodeInfo found = row == null ? null : firstCheckable(row);
                if (found != null) {
                    if (!reportedSettingGeometry) {
                        reportSettingGeometry(label, row);
                        reportedSettingGeometry = true;
                    }
                    return found;
                }
                reportSettingGeometry(label, row);
            }
            // This is the final Settings item. Its label may be visible while its trailing
            // Switch is still clipped; reaching the list end exposes the whole actual row.
            if (!scroll(true)) break;
            waitForIdleSync();
            SystemClock.sleep(250);
        }
        throw new AssertionError("Named developer row must expose visible checkable state after showing the complete row");
    }

    private AccessibilityNodeInfo firstCheckable(AccessibilityNodeInfo node) {
        if (node.isVisibleToUser() && node.isCheckable()) return node;
        for (int index = 0; index < node.getChildCount(); index++) {
            AccessibilityNodeInfo child = node.getChild(index);
            AccessibilityNodeInfo found = child == null ? null : firstCheckable(child);
            if (found != null) return found;
        }
        return null;
    }

    private AccessibilityNodeInfo clickableAncestor(AccessibilityNodeInfo node) {
        while (node != null && !node.isClickable()) node = node.getParent();
        return node;
    }

    private void reportSettingGeometry(AccessibilityNodeInfo label, AccessibilityNodeInfo row) {
        StringBuilder geometry = new StringBuilder("Known developer row structural metadata:\n");
        AccessibilityNodeInfo ancestor = label;
        for (int depth = 0; ancestor != null && depth < 5; depth++, ancestor = ancestor.getParent()) {
            appendNodeGeometry(geometry, "ancestor" + depth, ancestor);
        }
        if (row != null) appendSubtreeGeometry(geometry, row, 0, new int[]{0});
        Bundle status = new Bundle();
        status.putString("stream", geometry.toString());
        sendStatus(0, status);
    }

    private void appendSubtreeGeometry(StringBuilder output, AccessibilityNodeInfo node, int depth, int[] count) {
        if (depth > 5 || count[0]++ >= 40) return;
        appendNodeGeometry(output, "row-child-depth" + depth, node);
        for (int index = 0; index < node.getChildCount(); index++) {
            AccessibilityNodeInfo child = node.getChild(index);
            if (child != null) appendSubtreeGeometry(output, child, depth + 1, count);
        }
    }

    private void appendNodeGeometry(StringBuilder output, String position, AccessibilityNodeInfo node) {
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        output.append(position).append(" class=").append(node.getClassName())
                .append(" checkable=").append(node.isCheckable())
                .append(" checked=").append(node.isChecked())
                .append(" visible=").append(node.isVisibleToUser())
                .append(" bounds=").append(bounds.toShortString()).append('\n');
    }

    private AccessibilityNodeInfo hierarchyCheckbox() {
        for (AccessibilityNodeInfo root : applicationRoots()) {
            AccessibilityNodeInfo found = checkbox(root);
            if (found != null) return found;
        }
        throw new AssertionError("Inspector hierarchy checkbox must be visible");
    }

    private AccessibilityNodeInfo checkbox(AccessibilityNodeInfo node) {
        if (node.isVisibleToUser() && "android.widget.CheckBox".contentEquals(node.getClassName())) return node;
        for (int index = 0; index < node.getChildCount(); index++) {
            AccessibilityNodeInfo child = node.getChild(index);
            AccessibilityNodeInfo found = child == null ? null : checkbox(child);
            if (found != null) return found;
        }
        return null;
    }

    private String copyLocator() throws Exception {
        click(COPY, false);
        AtomicReference<String> copied = new AtomicReference<>();
        // Clipboard is read only after the actual Copy button explicitly overwrites it.
        runOnMainSync(() -> {
            ClipboardManager clipboard = getTargetContext().getSystemService(ClipboardManager.class);
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0 && clip.getItemAt(0).getText() != null) {
                copied.set(clip.getItemAt(0).getText().toString());
            }
        });
        require(copied.get() != null, "Actual Copy must publish a text locator");
        return copied.get();
    }

    private void click(String label, boolean scroll) throws Exception {
        AccessibilityNodeInfo node = find(label, scroll);
        while (node != null && !node.isClickable()) node = node.getParent();
        require(node != null && node.isEnabled(), "Known control must have an enabled clickable ancestor: " + label);
        require(node.performAction(AccessibilityNodeInfo.ACTION_CLICK), "Known control must accept click: " + label);
        waitForIdleSync();
        SystemClock.sleep(180);
    }

    private void shortTap(AccessibilityNodeInfo node) {
        Rect bounds = bounds(node);
        long down = SystemClock.uptimeMillis();
        pointer(down, MotionEvent.ACTION_DOWN, bounds.exactCenterX(), bounds.exactCenterY());
        SystemClock.sleep(70);
        pointer(down, MotionEvent.ACTION_UP, bounds.exactCenterX(), bounds.exactCenterY());
        waitForIdleSync();
    }

    private void deepHold(AccessibilityNodeInfo node) {
        Rect bounds = bounds(node);
        long down = SystemClock.uptimeMillis();
        pointer(down, MotionEvent.ACTION_DOWN, bounds.exactCenterX(), bounds.exactCenterY());
        SystemClock.sleep(3_350);
        pointer(down, MotionEvent.ACTION_UP, bounds.exactCenterX(), bounds.exactCenterY());
        waitForIdleSync();
    }

    private Rect bounds(AccessibilityNodeInfo node) {
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        require(!bounds.isEmpty() && node.isVisibleToUser(), "Known target must have visible measured bounds");
        return bounds;
    }

    private void pointer(long down, int action, float x, float y) {
        MotionEvent.PointerProperties properties = new MotionEvent.PointerProperties();
        properties.id = 0;
        properties.toolType = MotionEvent.TOOL_TYPE_FINGER;
        MotionEvent.PointerCoords coordinates = new MotionEvent.PointerCoords();
        coordinates.x = x;
        coordinates.y = y;
        coordinates.pressure = action == MotionEvent.ACTION_UP ? 0f : 1f;
        coordinates.size = 1f;
        MotionEvent event = MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, 1,
                new MotionEvent.PointerProperties[]{properties}, new MotionEvent.PointerCoords[]{coordinates},
                0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        try {
            sendPointerSync(event);
        } finally {
            event.recycle();
        }
    }

    private AccessibilityNodeInfo find(String label, boolean scroll) throws Exception {
        long deadline = SystemClock.uptimeMillis() + 15_000;
        long start = SystemClock.uptimeMillis();
        boolean forward = true;
        while (SystemClock.uptimeMillis() < deadline) {
            AccessibilityNodeInfo match = findNow(label);
            if (match != null) return match;
            if (scroll && SystemClock.uptimeMillis() - start > 500) {
                if (!scroll(forward)) forward = false;
            }
            SystemClock.sleep(150);
        }
        throw new AssertionError("Known English control unavailable: " + label);
    }

    private AccessibilityNodeInfo findNow(String label) {
        for (AccessibilityNodeInfo root : applicationRoots()) {
            AccessibilityNodeInfo found = matchingNode(root, label);
            if (found != null) return found;
        }
        return null;
    }

    private AccessibilityNodeInfo matchingNode(AccessibilityNodeInfo node, String label) {
        if (node.isVisibleToUser() && (matches(node.getText(), label)
                || matches(node.getContentDescription(), label) || matches(node.getHintText(), label))) return node;
        for (int index = 0; index < node.getChildCount(); index++) {
            AccessibilityNodeInfo child = node.getChild(index);
            AccessibilityNodeInfo found = child == null ? null : matchingNode(child, label);
            if (found != null) return found;
        }
        return null;
    }

    private static boolean matches(CharSequence value, String knownLabel) {
        if (value == null) return false;
        for (String line : value.toString().split("\\n")) if (knownLabel.equals(line)) return true;
        return false;
    }

    private List<AccessibilityNodeInfo> applicationRoots() {
        if (Build.VERSION.SDK_INT >= 33) automation.clearCache();
        SparseArray<List<AccessibilityWindowInfo>> displays = automation.getWindowsOnAllDisplays();
        List<AccessibilityWindowInfo> windows = new ArrayList<>();
        for (int index = 0; index < displays.size(); index++) windows.addAll(displays.valueAt(index));
        windows.sort(Comparator.comparing(AccessibilityWindowInfo::isFocused).reversed()
                .thenComparing(Comparator.comparingInt(AccessibilityWindowInfo::getLayer).reversed()));
        List<AccessibilityNodeInfo> roots = new ArrayList<>();
        for (AccessibilityWindowInfo window : windows) {
            if (window.getType() != AccessibilityWindowInfo.TYPE_APPLICATION) continue;
            AccessibilityNodeInfo root = window.getRoot();
            if (root != null && root.getPackageName() != null && targetPackage.contentEquals(root.getPackageName())) roots.add(root);
        }
        return roots;
    }

    private boolean hasApplicationWindow(int id) {
        for (AccessibilityNodeInfo root : applicationRoots()) if (root.getWindowId() == id) return true;
        return false;
    }

    private AccessibilityNodeInfo findInNewWindow(String label, List<Integer> previousWindows) {
        for (AccessibilityNodeInfo root : applicationRoots()) {
            if (!previousWindows.contains(root.getWindowId())) {
                AccessibilityNodeInfo found = matchingNode(root, label);
                if (found != null) return found;
            }
        }
        return null;
    }

    private boolean scroll(boolean forward) {
        AccessibilityNodeInfo best = null;
        int area = 0;
        for (AccessibilityNodeInfo root : applicationRoots()) {
            List<AccessibilityNodeInfo> candidates = new ArrayList<>();
            collectScrollers(root, candidates);
            for (AccessibilityNodeInfo node : candidates) {
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                int candidateArea = bounds.width() * bounds.height();
                if (candidateArea > area) { best = node; area = candidateArea; }
            }
        }
        return best != null && best.performAction(forward
                ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
    }

    private void collectScrollers(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> found) {
        if (node.isVisibleToUser() && node.isScrollable()
                && !"android.widget.EditText".contentEquals(node.getClassName())) found.add(node);
        for (int index = 0; index < node.getChildCount(); index++) {
            AccessibilityNodeInfo child = node.getChild(index);
            if (child != null) collectScrollers(child, found);
        }
    }

    private void writeEvidence(String suffix, byte[] payload) throws Exception {
        require(Arrays.asList("settings.xml", "modal.xml", "settings.png").contains(suffix), "Static synthetic evidence name");
        ParcelFileDescriptor[] pipes = automation.executeShellCommandRw("tee /data/local/tmp/foodus-story29-minified-" + suffix);
        AtomicReference<Throwable> writerFailure = new AtomicReference<>();
        Thread writer = new Thread(() -> {
            try (OutputStream stream = new ParcelFileDescriptor.AutoCloseOutputStream(pipes[1])) {
                stream.write(payload);
            } catch (Throwable error) {
                writerFailure.set(error);
            }
        }, "foodus-structural-evidence");
        writer.start();
        ByteArrayOutputStream echo = new ByteArrayOutputStream();
        try (InputStream input = new ParcelFileDescriptor.AutoCloseInputStream(pipes[0])) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) echo.write(buffer, 0, count);
        }
        writer.join(5_000);
        require(!writer.isAlive() && writerFailure.get() == null, "Write synthetic minified evidence bytes");
        require(Arrays.equals(payload, echo.toByteArray()), "Persist exact actual minified evidence bytes");
    }

    private void saveSettingsScreenshot() throws Exception {
        Bitmap screenshot = automation.takeScreenshot();
        require(screenshot != null, "Capture actual synthetic Settings screenshot");
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        require(screenshot.compress(Bitmap.CompressFormat.PNG, 100, png), "Encode Settings PNG");
        screenshot.recycle();
        writeEvidence("settings.png", png.toByteArray());
    }

    private void checkpoint(String name) {
        Bundle status = new Bundle();
        status.putString("stream", "FoodUs minified platform inspection: " + name + "\n");
        sendStatus(0, status);
    }

    private interface Condition { boolean evaluate() throws Exception; }

    private void await(String label, Condition condition) throws Exception {
        long deadline = SystemClock.uptimeMillis() + 10_000;
        while (SystemClock.uptimeMillis() < deadline) {
            if (condition.evaluate()) return;
            SystemClock.sleep(100);
        }
        throw new AssertionError("Timed out: " + label);
    }

    private static void contains(String actual, String expected, String label) {
        require(actual.contains(expected), label);
    }

    private static void equal(String expected, String actual, String label) {
        require(expected.equals(actual), label);
    }

    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}

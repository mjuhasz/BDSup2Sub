package bdsup2sub.gui.main;

import bdsup2sub.gui.BDSup2SubAcceptanceTest;

import org.fest.swing.annotation.GUITest;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.matcher.DialogMatcher;
import org.fest.swing.core.matcher.JButtonMatcher;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.JFileChooserFixture;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;

import java.io.File;

import static org.junit.Assert.assertTrue;

@Ignore
public class BDSup2SubMainWindowTest extends BDSup2SubAcceptanceTest {

    @GUITest
    @Test
    public void testFileOpenDialog() throws Exception {
        window.menuItemWithPath("File", "Load").click();

        JFileChooserFixture fileChooser = window.fileChooser();
        fileChooser.requireVisible();
        fileChooser.cancel();
    }

    @GUITest
    @Test
    public void testRecentFiles() throws Exception {
        File testFile = new File(ClassLoader.getSystemResource("test.sup").toURI());

        window.menuItemWithPath("File", "Load").click();
        window.fileChooser().selectFile(testFile).approve();
        closeConversionDialog();
        window.menuItemWithPath("File", "Close").click();

        window.menuItemWithPath("File", "Recent Files", "0: " + testFile.getAbsolutePath()).requireVisible().click();
        closeConversionDialog();
        window.menuItemWithPath("File", "Close").click();
    }

    private void closeConversionDialog() {
        DialogFixture dialog = window.dialog(new GenericTypeMatcher<JDialog>(JDialog.class) {
            @Override
            protected boolean isMatching(JDialog dialog) {
                return "Conversion Options".equals(dialog.getTitle()) && dialog.isShowing();
            }
        });
        dialog.requireVisible().requireModal();
        dialog.button(JButtonMatcher.withText("  Ok  ")).click();
    }

    @GUITest
    @Test
    public void testQuit() throws Throwable {
        class MyExitCallHook implements ExitCallHook {
            boolean exitCalled;
            @Override
            public void exitCalled(int status) {
                exitCalled = true;
            }
        }
        MyExitCallHook exitCallHook = new MyExitCallHook();
        NoExitSecurityManagerInstaller noExitSecurityManagerInstaller = NoExitSecurityManagerInstaller.installNoExitSecurityManager(exitCallHook);

        try {
            window.menuItemWithPath("File", "Quit").click();
        } finally {
            noExitSecurityManagerInstaller.uninstall();
        }

        assertTrue(exitCallHook.exitCalled);
    }

    @GUITest
    @Test
    public void testAboutDialog() throws Exception {
        window.menuItemWithPath("Help", "About").click();

        DialogFixture dialog = window.dialog(DialogMatcher.withTitle("BDSup2Sub"));
        dialog.requireVisible().requireModal();
        dialog.button(JButtonMatcher.withText("OK")).click();

        dialog.requireNotVisible();
    }
}

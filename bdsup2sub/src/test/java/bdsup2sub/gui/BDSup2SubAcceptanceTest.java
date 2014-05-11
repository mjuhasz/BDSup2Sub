package bdsup2sub.gui;

import bdsup2sub.BDSup2Sub;
import bdsup2sub.core.Configuration;
import bdsup2sub.gui.main.MainFrameView;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.junit.v4_5.runner.GUITestRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.io.File;

import static org.fest.swing.launcher.ApplicationLauncher.application;

@RunWith(GUITestRunner.class)
public abstract class BDSup2SubAcceptanceTest {
    protected FrameFixture window;
    protected Robot robot;

    @BeforeClass
    public static void setUpOnce() throws Exception {
        FailOnThreadViolationRepaintManager.install();
        deleteConfigFile();
    }

    @Before
    public void setUp() throws Exception {
        GuiActionRunner.execute(new GuiTask() {
            @Override
            protected void executeInEDT() throws Throwable {
                application(BDSup2Sub.class).start();
            }
        });
        robot = BasicRobot.robotWithCurrentAwtHierarchy();
        window = WindowFinder.findFrame(MainFrameView.class).using(robot);
        window.show();
    }

    @After
    public void tearDown() throws Exception {
        window.cleanUp();
    }

    @AfterClass
    public static void tearDownOnce() throws Exception {
        deleteConfigFile();
    }

    private static void deleteConfigFile() {
        new File(Configuration.getInstance().getConfigFilePath()).delete();
    }
}

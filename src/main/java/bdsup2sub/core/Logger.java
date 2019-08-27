/*
 * Copyright 2014 Miklos Juhasz (mjuhasz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bdsup2sub.core;

import bdsup2sub.gui.main.MainFrameView;

import javax.swing.*;

public final class Logger {

    private static final Configuration configuration = Configuration.getInstance();
    private static final Logger INSTANCE = new Logger();

    private int errorCount;
    private int warningCount;

    private MainFrameView mainFrame;

    private Logger() {
    }

    public static Logger getInstance() {
        return INSTANCE;
    }

    public void warn(String message) {
        warningCount++;
        final String msg = "WARNING: " + message;
        if (mainFrame != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mainFrame.printToConsole(msg);
                }
            });
        } else {
            System.err.print(msg);
        }
    }

    public void error(String message) {
        errorCount++;
        final String msg = "ERROR: " + message;
        if (mainFrame != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mainFrame.printToConsole(msg);
                }
            });
        } else {
            System.err.print(msg);
        }
    }

    public void trace(String message) {
        final String msg = message;
        if (configuration.isVerbose()) {
            if (mainFrame != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        mainFrame.printToConsole(msg);
                    }
                });
            } else {
                System.err.print(msg);
            }
        }
    }

    public void info(String message) {
        final String msg = message;
        if (mainFrame != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mainFrame.printToConsole(msg);
                }
            });
        } else {
            System.err.print(msg);
        }
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void resetErrorCounter() {
        errorCount = 0;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void resetWarningCounter() {
        warningCount = 0;
    }

    public void printWarningsAndErrorsAndResetCounters() {
        if (warningCount + errorCount > 0) {
            String message = "";
            if (warningCount > 0) {
                if (warningCount == 1) {
                    message += warningCount + " warning";
                } else {
                    message += warningCount + " warnings";
                }
            }
            if (warningCount > 0 && errorCount > 0) {
                message += " and ";
            }
            if (errorCount > 0) {
                if (errorCount == 1) {
                    message = errorCount + " error";
                } else {
                    message = errorCount + " errors";
                }
            }
            if (warningCount + errorCount < 3) {
                message = "There was " + message;
            } else {
                message = "There were " + message;
            }
            System.err.println(message);
        }
        resetWarningCounter();
        resetErrorCounter();
    }

    public void setMainFrame(MainFrameView mainFrame) {
        this.mainFrame = mainFrame;
    }
}

//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui;

import hexgui.gui.HexGui;
import hexgui.util.Options;
import hexgui.version.Version;

import javax.swing.*;          
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public final class Main
{
    final static String LOOKANDFEEL = "System";

    private static void initLookAndFeel() {
        String lookAndFeel = null;

        if (LOOKANDFEEL != null) {
            if (LOOKANDFEEL.equals("Metal")) {
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("System")) {
                lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("Motif")) {
                lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            } else if (LOOKANDFEEL.equals("GTK+")) { //new in 1.4.2
                lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            } else {
                System.err.println("Unexpected value of LOOKANDFEEL specified: "
                                   + LOOKANDFEEL);
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }

            try {
                UIManager.setLookAndFeel(lookAndFeel);
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't find class for specified look and feel:"
                                   + lookAndFeel);
                System.err.println("Did you include the L&F library in the class path?");
                System.err.println("Using the default look and feel.");
            } catch (UnsupportedLookAndFeelException e) {
                System.err.println("Can't use the specified look and feel ("
                                   + lookAndFeel
                                   + ") on this platform.");
                System.err.println("Using the default look and feel.");
            } catch (Exception e) {
                System.err.println("Couldn't get specified look and feel ("
                                   + lookAndFeel
                                   + "), for some reason.");
                System.err.println("Using the default look and feel.");
                e.printStackTrace();
            }
        }
    }

    private static void createAndShowGUI(File file, String command) {
        initLookAndFeel();
        JFrame.setDefaultLookAndFeelDecorated(true);
        HexGui app = new HexGui(file, command);
    }

    public static void main(String[] args) throws Exception {
        try
        {
            String options[] = {
                "config:",
                "program:",
                "help",
                "version"
            };
            Options opt = Options.parse(args, options);
            if (opt.contains("help")) {
                String helpText =
                    "Usage: hexgui [options] [file]\n" +
                    "Graphical user interface for Hex programs\n" +
                    "using the Hex Text Protocol.\n" +
                    "\n" +
                    "-config file   Read command line arguments from file\n" +
                    "-help          Display this help and exit\n" +
                    "-program       Command for Hex program to attach\n" +
                    "-version       Print version and exit\n";
                System.out.print(helpText);
                return;
            }
            if (opt.contains("version")) {
                System.out.println("HexGui " + Version.id + " "
                                   + Version.date);
                return;
            }
            final String command = opt.get("program", null);
            ArrayList<String> arguments = opt.getArguments();
            final File file;
            if (arguments.size() == 0)
                file = null;
            else if (arguments.size() == 1)
                file = new File(arguments.get(0));
            else
                throw new Exception("Only one argument allowed");
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        createAndShowGUI(file, command);
                    }
                });
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
}

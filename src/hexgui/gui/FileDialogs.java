// FileDialogs.java

package hexgui.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.prefs.Preferences;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import hexgui.sgf.GameFileFilter;
import hexgui.util.ErrorMessage;
import hexgui.util.Platform;
import hexgui.util.StringUtils;

/** File dialogs. */
public final class FileDialogs
{
    public static File showOpen(Component parent, String title)
    {
        return showFileChooser(parent, Type.FILE_OPEN, null, false, title);
    }

    public static File showOpenSgf(Component parent)
    {
        return showFileChooser(parent, Type.FILE_OPEN, null, true, null);
    }

    public static File showSave(Component parent, String title,
                                MessageDialogs messageDialogs)
    {
        return showFileChooserSave(parent, null, false, title,
                                   messageDialogs);
    }

    public static File showSaveSgf(Frame parent,
                                   MessageDialogs messageDialogs)
    {
        return showFileChooserSave(parent, s_lastFile, true, null,
                                   messageDialogs);
    }

    /** File selection, unknown whether for load or save. */
    public static File showSelectFile(Component parent, String title)
    {
        return showFileChooser(parent, Type.FILE_SELECT, s_lastFile, false,
                               title);
    }

    public static void setLastFile(File file)
    {
        s_lastFile = file;
    }

    private enum Type
    {
        /** Dialog type for opening a file. */
        FILE_OPEN,

        /** Dialog type for saving to a file. */
        FILE_SAVE,

        /** Dialog type for selecting a file.
            Use this type, if a file name should be selected, but it is not
            known what the file name is used for and if the file already
            exists.
            @deprecated Not supported by native AWT FileDialog */
        FILE_SELECT
    }

    /** Use native AWT-dialogs.
        They are used on Mac OS X because JFileChooser looks too different
        from the native dialogs (Java 1.5), and on Windows because
        JFileChooser is too slow on Windows XP (startup and directory changing
        takes up to 10 sec; Java 1.6) */
    private static final boolean NATIVE_DIALOGS =
        (Platform.isMac() || Platform.isWindows());

    private static File s_lastFile;

    /** Make constructor unavailable; class is for namespace only. */
    private FileDialogs()
    {
    }

    /** Find first parent that is a Frame.
        @return null If no such parent. */
    private static Frame findParentFrame(Component component)
    {
        while (component != null)
            if (component instanceof Frame)
                return (Frame)component;
            else
                component = component.getParent();
        return null;
    }

    private static File showFileChooser(Component parent, Type type,
                                        File lastFile, boolean setSgfFilter,
                                        String title)
    {
        // Use native dialogs for some platforms. but not for type select
        // There is no native dialog for select
        if (NATIVE_DIALOGS && type != Type.FILE_SELECT)
        {
            Frame frame = findParentFrame(parent);
            return showFileChooserAWT(frame, type, title);
        }
        return showFileChooserSwing(parent, type, lastFile, setSgfFilter,
                                    title);
    }

    private static File showFileChooserSave(Component parent,
                                            File lastFile,
                                            boolean setSgfFilter,
                                            String title,
                                            MessageDialogs messageDialogs)
    {
        File file = showFileChooser(parent, Type.FILE_SAVE, lastFile,
                                    setSgfFilter, title);
        if (NATIVE_DIALOGS)
            // Overwrite warning is already part of FileDialog
            return file;
        while (file != null)
        {
            if (file.exists())
            {
                String mainMessage =
                    MessageFormat.format("Replace file \"{0}\"",
                                         file.getName());
                String optionalMessage = "If you overwrite the file, the previous version will be lost.";
                if (! messageDialogs.showQuestion(parent, mainMessage,
                                                  optionalMessage,
                                                  "Replace", true))
                {
                    file = showFileChooser(parent, Type.FILE_SAVE, lastFile,
                                           setSgfFilter, title);
                    continue;
                }
            }
            break;
        }
        return file;
    }

    private static File showFileChooserAWT(Frame parent, Type type,
                                           String title)
    {
        FileDialog dialog = new FileDialog(parent);
        if (title == null)
        {
            switch (type)
            {
            case FILE_OPEN:
                title = "Open";
                break;
            case FILE_SAVE:
                title = "Save";
                break;
            default:
                assert false;
            }
        }
        dialog.setTitle(title);
        int mode = FileDialog.LOAD;
        if (type == Type.FILE_SAVE)
            mode = FileDialog.SAVE;
        dialog.setMode(mode);
        /* Commented out, because there is no way to change the filter by the
           user (at least not on Linux)
        if (setSgfFilter)
            dialog.setFilenameFilter(new FilenameFilter() {
                    public boolean accept(File dir, String name)
                    {
                        return name.toLowerCase().endsWith("sgf");
                    }
                });
        */
        //dialog.setLocationRelativeTo(parent); // Java <= 1.4
        dialog.setLocationByPlatform(true);
        dialog.setVisible(true);
        if (dialog.getFile() == null)
            return null;
        return new File(dialog.getDirectory(), dialog.getFile());
    }

    private static File showFileChooserSwing(Component parent, Type type,
                                             File lastFile,
                                             boolean setSgfFilter,
                                             String title)
    {
        JFileChooser chooser;
        if (s_lastFile == null)
        {
            if (Platform.isMac())
                // user.dir is application directory on Mac, which is bad
                // I have not found a way to set it to user home in Info.plist
                // so I use null here, which sets is to the user home
                chooser = new JFileChooser((String)null);
            else
                chooser = new JFileChooser(System.getProperty("user.dir"));
        }
        else
            chooser = new JFileChooser(s_lastFile);
        chooser.setMultiSelectionEnabled(false);
        javax.swing.filechooser.FileFilter filter = new GameFileFilter();
        chooser.addChoosableFileFilter(filter);
        if (setSgfFilter)
        {
            chooser.setFileFilter(filter);
        }
        else
            chooser.setFileFilter(chooser.getAcceptAllFileFilter());
        if (type == Type.FILE_SAVE)
        {
            if (lastFile != null && lastFile.isFile() && lastFile.exists())
                chooser.setSelectedFile(lastFile);
        }
        if (title != null)
            chooser.setDialogTitle(title);
        int ret;
        switch (type)
        {
        case FILE_SAVE:
            ret = chooser.showSaveDialog(parent);
            break;
        case FILE_OPEN:
            ret = chooser.showOpenDialog(parent);
            break;
        default:
            ret = chooser.showDialog(parent, "Select");
            break;
        }
        if (ret != JFileChooser.APPROVE_OPTION)
            return null;
        File file = chooser.getSelectedFile();
        s_lastFile = file;
        return file;
    }
}

package hexgui.gui;

import java.util.*;
import javax.swing.*;

/** Runnable that is guaranteed to be run in the Swing event dispatch thread.
    Most Swing function may only be called in the Swing event dispatch
    thread. */
public class GuiRunnable
    implements Runnable
{
    public GuiRunnable(Runnable runnable)
    {
        m_runnable = runnable;
    }

    public void run()
    {
        SwingUtilities.invokeLater(m_runnable);
    }

    private Runnable m_runnable;
}

//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.game;

import hexgui.hex.*;
import hexgui.util.Pair;
import hexgui.util.StringUtils;

import java.io.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

public class Clock
    implements ActionListener
{
    public interface Listener
    {
        public void clockChanged();
    }

    /** Initializes a clock. */
    public Clock()
    {
        m_timer = new Timer(1000, this);
        m_elapsed = 0;
        m_startTime = -1;
    }

    public void addListener(Listener ls)
    {
        m_listener = ls;
    }
    
    public void start()
    {
        if (m_startTime != -1) // already started
            return;
        m_startTime = System.currentTimeMillis();
        m_timer.setInitialDelay((m_elapsed+999)/1000*1000 - m_elapsed);
        m_timer.start();
    }

    public void stop()
    {
        if (m_startTime == -1)  // already stopped
            return;
        m_timer.stop();
        m_elapsed += (int)(System.currentTimeMillis() - m_startTime);
        m_startTime = -1;
    }

    /** Returns elapsed time in miliseconds. */
    public int elapsed()
    {
        if (m_startTime == -1)
            return m_elapsed;
        return m_elapsed + (int)(System.currentTimeMillis() - m_startTime);
    }

    /** Sets the time. */
    public void setElapsed(int millis)
    {
        m_elapsed = millis;
        m_listener.clockChanged();
    }

    public void actionPerformed(ActionEvent e)
    {
        if (m_listener != null)
            m_listener.clockChanged();
    }

    private Listener m_listener;

    private Timer m_timer;

    private int m_elapsed;
    
    private long m_startTime;
};

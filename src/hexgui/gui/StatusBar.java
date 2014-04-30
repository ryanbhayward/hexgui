//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.gui;

import java.io.*;
import java.util.*;
import javax.swing.*;          
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;

//----------------------------------------------------------------------------

public class StatusBar
    extends JPanel
{
    public StatusBar()
    {
        super();
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(0,25));
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        //setLayout(new FlowLayout(FlowLayout.RIGHT));

        m_message = new JLabel();
        m_message.setHorizontalTextPosition(JLabel.LEFT);
        add(m_message, BorderLayout.WEST);

        m_progress = new JProgressBar(0, 1000000);
        m_progress.setValue(0);
        m_progress.setMinimumSize(new Dimension(100, 25));
        m_progress.setStringPainted(true);
        m_progress.setString("");
        m_progress.setVisible(false);
        add(m_progress, BorderLayout.EAST);

        setMessage("Ready");
        setVisible(true);
    }

    public void setMessage(String msg)
    {
        assert SwingUtilities.isEventDispatchThread();
        m_message.setText(msg);
    }

    public void setProgressVisible(boolean visible)
    {
        assert SwingUtilities.isEventDispatchThread();
        m_progress.setVisible(visible);
    }

    public void setProgress(double pct)
    {
        assert SwingUtilities.isEventDispatchThread();
        m_progress.setValue((int)(pct*1000000.0));

        // show 4 decimal places of accuracy
        NumberFormat nf = NumberFormat.getInstance();
        if (nf instanceof DecimalFormat) 
            ((DecimalFormat)nf).applyPattern("##0.0000");

        m_progress.setString(nf.format(pct*100.0)+"%");
    }

    JLabel m_message;
    JProgressBar m_progress;
}

//----------------------------------------------------------------------------

//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.gui;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;

/** Displays comment for current node. */
public class Comment 
    extends JScrollPane
    implements DocumentListener
{
   
    public interface Listener
    {
        public void commentChanged(String msg);
    }

    public Comment(Listener listener)
    {
        m_listener = listener;
        m_textPane = new JTextArea();
        m_textPane.setFont(MONOSPACED_FONT);
        setViewportView(m_textPane);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        m_textPane.getDocument().addDocumentListener(this);
        //setPreferredSize(new Dimension(200, 400));
    }

    public void setText(String text)
    {
        m_textPane.setText(text);
        m_textPane.getCaret().setDot(0);
    }

    public void changedUpdate(DocumentEvent e)
    {
        notifyChanged();
    }

    public void removeUpdate(DocumentEvent e)
    {
        notifyChanged();
    }

    public void insertUpdate(DocumentEvent e)
    {
        notifyChanged();
    }

    private void notifyChanged()
    {
        m_listener.commentChanged(m_textPane.getText());
    }

    JTextArea m_textPane;

    Listener m_listener;

    private static final Font MONOSPACED_FONT = Font.decode("Monospaced");
}

//----------------------------------------------------------------------------

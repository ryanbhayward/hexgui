//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.gui;

import hexgui.hex.*;
import hexgui.htp.HtpController;

import javax.swing.*;          
import javax.swing.text.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

//----------------------------------------------------------------------------

/** Non-modal dialog displaying list of VCs.  Clicking on a vc displays
    it to the given GuiBoard. */
public class VCDisplayDialog 
    extends JDialog implements ListSelectionListener,
                               FocusListener
{
    public VCDisplayDialog(JFrame owner, GuiBoard board, Vector<VC> vcs)
    {
	super(owner, "HexGui: VCs");

	addWindowListener(new WindowAdapter() 
	    {
		public void windowClosing(WindowEvent winEvt) {
		    dispose();
		}
	    });

        m_guiboard = board;

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        m_list = new JList();
        m_list.addFocusListener(this);
        m_list.addListSelectionListener(this);
        m_list.setDragEnabled(false);
        m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	m_scrollpane = new JScrollPane(m_list);
	m_scrollpane.setVerticalScrollBarPolicy(
		     JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(m_scrollpane);
        add(panel);

        pack();

	Dimension size = owner.getSize();
	setLocation(0, size.height);

        setVCs(vcs);
        setVisible(true);
    }

    public void setVCs(Vector<VC> vcs)
    {
        m_vcs = vcs;
        m_list.setListData(vcs);
    }

    public void valueChanged(ListSelectionEvent e)
    {
        if (m_list.isSelectionEmpty())
            return;

        VC vc = m_vcs.get(m_list.getSelectedIndex());
        if (vc.getType().equals("softlimit")) // do nothing on this
            return;
        m_guiboard.clearMarks();
        m_guiboard.displayVC(vc);
        m_guiboard.repaint();
    }
    
    public void focusGained(FocusEvent e)
    {
    }

    public void focusLost(FocusEvent e)
    {
        m_list.clearSelection();
    }

    private JList m_list;
    private JScrollPane m_scrollpane;
    private Vector<VC> m_vcs;
    private GuiBoard m_guiboard;
}

//----------------------------------------------------------------------------

//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.gui;

import hexgui.hex.*;
import hexgui.htp.HtpController;

import javax.swing.*;          
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

/** Non-modal dialog displaying the communication between HexGui and a 
    HTP compatible program. */
public class HtpShell 
    extends JDialog implements ActionListener, HtpController.IOInterface
{
    public interface Callback 
    {
	void commandEntered(String str);
    }

    public HtpShell(JFrame owner, Callback callback)
    {
	super(owner, "HexGui: Shell");
	m_callback = callback;

	m_editor = new JTextPane();
	m_editor.setEditable(false);
	m_document = m_editor.getStyledDocument();
	addStylesToDocument(m_document);

	m_scrollpane = new JScrollPane(m_editor);
	m_scrollpane.setVerticalScrollBarPolicy(
		     JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

	Dimension size = owner.getSize();
	getContentPane().add(m_scrollpane, BorderLayout.CENTER);

	setPreferredSize(new Dimension(400, size.height));
	setMinimumSize(new Dimension(400, 200));
	setLocation(size.width, 0);

	m_field = new JTextField();
	m_field.addActionListener(this);
	m_field.setActionCommand("command-entered");
	getContentPane().add(m_field, BorderLayout.SOUTH);	
	
	pack();
    }

    public void appendText(String text)
    {
	appendText(text, null);
    }

    public void appendText(String text, AttributeSet style)
    {
	try {
	    m_document.insertString(m_document.getLength(), text, style);
	} 
	catch (BadLocationException e) {
	    System.out.println("Bad location!");
	}
    }


    /** HtpController.IOInterface */
    public void sentCommand(String str)
    {
	appendText(str, m_document.getStyle("blue"));
    }

    public void receivedResponse(String str)
    {
	appendText(str, m_document.getStyle("bold"));
    }

    public void receivedError(String str)
    {
	appendText(str, m_document.getStyle("red"));
    }

    protected void addStylesToDocument(StyledDocument doc) {
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);
 
        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "Monospaced");
	StyleConstants.setFontSize(def, 12);
 
        Style s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);
 
        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);

	s = doc.addStyle("blue", regular);
	StyleConstants.setForeground(s, Color.blue);
 
	s = doc.addStyle("red", regular);
	StyleConstants.setForeground(s, Color.RED);

	s = doc.addStyle("gray", regular);
	StyleConstants.setForeground(s, Color.gray);

	s = doc.addStyle("yellow", regular);
	StyleConstants.setForeground(s, Color.YELLOW);	
    }

    public void actionPerformed(ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("command-entered")) {
	    String text = m_field.getText() + "\n";
	    m_callback.commandEntered(text);
	    m_field.setText(null);
	}
    }

    JTextPane m_editor;
    JTextField m_field;
    JScrollPane m_scrollpane;
    StyledDocument m_document;
    Callback m_callback;
}


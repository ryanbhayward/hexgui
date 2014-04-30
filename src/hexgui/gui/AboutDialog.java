package hexgui.gui;

import hexgui.version.Version;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;          
import javax.swing.border.EtchedBorder;
import javax.swing.text.html.HTMLEditorKit;

//----------------------------------------------------------------------------

/** Shows info about HexGui. */
public class AboutDialog 
    extends JDialog implements ActionListener
{
    /** Display modal about dialog. */
    public AboutDialog(Frame owner)
    {
        super(owner, true);
        setTitle("About HexGui");
        //setPreferredSize(new Dimension(400,300));
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel tp = new JPanel();
        tp.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        
        String benzeneUrl = "http://benzene.sf.net";
        String goguiUrl = "http://gogui.sf.net";
        String about = "<html><body>" 
            + "<p align=\"center\"><img src=\"" 
            + getImage("hexgui-48x48-notrans.png") + "\"></p>" 
            + "<p align=\"center\"><b>HexGui</b></p>"
            + "<p align=\"center\">Version " + Version.id + "</p>"
            + "<p align=\"center\">Graphical user interface for Hex programs<br>"
            + "(C) 2006-2011 Broderick Arneson.<br>" 
            + "<a href=\"" + benzeneUrl + "\">" + benzeneUrl + "</a></p>"
            + "<p align=\"center\">HexGui is based in large part on<br>"
            + "<b>GoGui</b> by Markus Enzenberger<br>"
            + "<a href=\"" + goguiUrl + "\">" + goguiUrl + "</a></p>"
            + "<p align=\"center\"><b>HexGui</b> is full of Hexy Goodness!</p>"
            + "</body></html>";

        JEditorPane text = new JEditorPane();
        text.setEditable(false);
        text.setEditorKit(new HTMLEditorKit());
        text.setText(about);
            
        tp.add(text);

        JButton button = new JButton("OK");
        button.setActionCommand("OK");
        button.addActionListener(this);

        panel.add(tp);
        panel.add(button);
        add(panel);

        pack();
    }

    public void actionPerformed(ActionEvent e)
    {
        setVisible(false);
    }

    private URL getImage(String name)
    {
        ClassLoader loader = getClass().getClassLoader();
        return loader.getResource("hexgui/images/" + name);
    }
}

//----------------------------------------------------------------------------

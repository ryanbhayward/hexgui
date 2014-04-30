//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.gui;

import hexgui.hex.*;

import java.util.Vector;
import javax.swing.*;          
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.*;

/** The actual dialog. */
class ChooseProgramDialog extends JDialog implements ActionListener
{
    public ChooseProgramDialog(Frame owner, String title, Vector<Program> programs)
    {
        super(owner, true);
        setTitle(title);

        m_programs = programs;

        // create gui
        JEditorPane info = new JEditorPane();
        info.setEditable(false);
        info.setEditorKit(new HTMLEditorKit());
        info.setText("Select program from list below.");
        add(info, BorderLayout.NORTH);
        add(createProgramPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        pack();
        
        setResizable(false);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if (cmd.equals("OK")) {
            
            m_program = m_programs.get(m_list.getSelectedIndex());
            
            // set the program
            setVisible(false);
            
        } else if (cmd.equals("Cancel")) {
            setVisible(false);
        }
    }
    
    public Program getProgram()
    {
        return m_program;
    }
    
    private JPanel createProgramPanel()
    {
        JPanel panel = new JPanel();
        
        m_list = new JList(m_programs);
        m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_list.setVisibleRowCount(10);
        m_list.setSelectedIndex(0);
        m_list.setPreferredSize(new Dimension(200, 250));
        
        JScrollPane sc = new JScrollPane(m_list);
        sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(sc);
        
        return panel;
    }
    
    private JPanel createButtonPanel()
    {
        JPanel panel = new JPanel();
        
        JButton button = new JButton("  OK  ");
        button.addActionListener(this);
        button.setActionCommand("OK");
        panel.add(button);
        
        button = new JButton("Cancel");
        button.addActionListener(this);
        button.setActionCommand("Cancel");
        panel.add(button);
        
        return panel;
    }
    
    JList m_list;
    
    Vector<Program> m_programs;
    Program m_program;
}

//----------------------------------------------------------------------------

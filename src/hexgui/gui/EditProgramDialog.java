//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.gui;

import hexgui.util.SpringUtilities;
import hexgui.gui.ShowError;
import hexgui.gui.Program;

import java.util.Vector;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.*;

/** Dialog for adding/editing new programs. */
public final class EditProgramDialog
    extends JDialog implements ActionListener
{

    public EditProgramDialog(Frame owner, Program program, 
                            String title, boolean is_new)
    {
        super(owner, true);
        m_program = program;

        setTitle(title);
        init(is_new);
    }

    private void init(boolean is_new)
    {
        JEditorPane info = new JEditorPane();
        info.setEditable(false);
        info.setEditorKit(new HTMLEditorKit());
        
        if (!is_new) {
            info.setText("Edit the program's fields.");
        } else {
            info.setText("<h3>Enter command for new Hex program</h3>"+
                         "<p>The command can be simply the name of the " +
                         "executable file, or the name plus any options " +
                         "you wish to set.  The working directory can be left " +
                         "blank if the program does not need a special " +
                         "working directory. Enter a simple descriptive name " +
                         "to refer to this program.");
        }
        add(info, BorderLayout.NORTH);
        add(createProgramPanel(m_program), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        if (!is_new) {
            setPreferredSize(new Dimension(500, 180));
        } else {
            setPreferredSize(new Dimension(500, 280));
        }
        pack();

        setResizable(false);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if (cmd.equals("OK")) {

            m_program.m_name = m_name.getText();
            m_program.m_command = m_command.getText();
            m_program.m_working = m_working.getText();
           
            dispose();

        } else if (cmd.equals("Cancel")) {
            dispose();
        }
    }

    private JPanel createProgramPanel(Program program)
    {
        JPanel panel = new JPanel(new SpringLayout());
        JLabel l;

        l = new JLabel("Name:", JLabel.TRAILING);
        panel.add(l);

        m_name = new JTextField(40);
        if (program != null) m_name.setText(program.m_name);
        l.setLabelFor(m_name);
        panel.add(m_name);

        l = new JLabel("Command:", JLabel.TRAILING);
        panel.add(l);
        m_command = new JTextField(40);
        if (program != null) m_command.setText(program.m_command);
        l.setLabelFor(m_command);
        panel.add(m_command);

        l = new JLabel("Working Directory:", JLabel.TRAILING);
        panel.add(l);
        m_working = new JTextField(40);
        if (program != null) m_working.setText(program.m_working);
        l.setLabelFor(m_working);
        panel.add(m_working);

        SpringUtilities.makeCompactGrid(panel,
                                        3, 2,        // rows, cols
                                        6, 6,        // initX, initY
                                        6, 6);       // xPad, yPad

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

    JTextField m_name;
    JTextField m_command;
    JTextField m_working;

    Program m_program;
}

//----------------------------------------------------------------------------

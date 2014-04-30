//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.gui;

import hexgui.hex.*;
import javax.swing.*;          
import java.awt.*;
import java.awt.event.*;

/** Dialog for changes user preferences.
  */
public final class PreferencesDialog 
    extends JDialog implements ItemListener, ActionListener
{
    public PreferencesDialog(Frame owner, GuiPreferences preferences)
    {
        super(owner, true);

        m_preferences = preferences;

        JPanel generalPanel = createGeneralPanel();
        JPanel boardPanel = createBoardPanel();
        JPanel drawPanel = createDrawPanel();
        JPanel buttonPane = createButtonPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", null,
                          generalPanel,
                          "General preferences");
        tabbedPane.addTab("Board", null,
                          boardPanel,
                          "Board preferences");
        tabbedPane.addTab("Draw", null,
                          drawPanel, 
                          "Drawing preferences");

        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.SOUTH);
        pack();

        setVisible(true);
    }

    public void itemStateChanged(ItemEvent e)
    {
        System.out.println("ItemEvent!");
        Object source = e.getItemSelectable();

    }

    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if (cmd.equals("OK")) {
            savePreferences();
            dispose();
        } else if (cmd.equals("Cancel")) {
            dispose();
        }
    }

    private void savePreferences()
    {
        System.out.println("Saving preferences...");

        m_preferences.put("shell-show-on-connect", 
                          (showShellOnConnect.getSelectedObjects() != null));
        m_preferences.put("analyze-show-on-connect", 
                          (showAnalyzeOnConnect.getSelectedObjects() != null));
        m_preferences.put("auto-respond", 
                          (autoRespond.getSelectedObjects() != null));
        
    }

    private JPanel createGeneralPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        showShellOnConnect = createCheckBox("Show Shell on Program Connect",
                                            "shell-show-on-connect");

        showAnalyzeOnConnect = createCheckBox("Show Analyze on Program Connect",
                                            "analyze-show-on-connect");

        autoRespond = createCheckBox("Auto-respond", "auto-respond");

        panel.add(showShellOnConnect);
        panel.add(showAnalyzeOnConnect);
        panel.add(autoRespond);

        return panel;
    }

    private JPanel createDrawPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

//         JLabel label;

//         label = new JLabel("Field alpha percentage");
//         SpinnerNumberModel model = 
//             new SpinnerNumberModel(m_preferences.getDouble("");
//         fieldAlpha = new JSpinner(new );

        return panel;
    }


    private JPanel createBoardPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Board stuff"));
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

    private JCheckBox createCheckBox(String name, String prefname)
    {
        JCheckBox box = new JCheckBox(name);
        box.setSelected(m_preferences.getBoolean(prefname));
        box.addItemListener(this);
        return box;
    }

    JCheckBox showShellOnConnect, showAnalyzeOnConnect;
    JCheckBox autoRespond;

    JSpinner fieldAlpha;

    GuiPreferences m_preferences;
}


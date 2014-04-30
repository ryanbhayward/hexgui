//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.gui;

import java.awt.Component;
import javax.swing.JOptionPane;

/** Displays a simple error message dialog. */ 
public class ShowError
{
    public static void msg(Component parent, String msg)
    {
	JOptionPane.showMessageDialog(parent, msg, "Error",
				      JOptionPane.ERROR_MESSAGE);
    }

    /** No constructor; class is for namespace only. */
    private ShowError() 
    { 
    }
}

//----------------------------------------------------------------------------

//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.gui;

import hexgui.hex.*;
import javax.swing.*;          
import java.awt.*;
import java.awt.event.*;

/** Dialog for entering a remote program hostname. 
    FIXME: allow port to be set. 
  */
public final class RemoteProgramDialog
{
    /** Run dialog.
        @return command to run.
	if user aborted.
    */
    public static String show(Component parent, String value)
    {
        String ret = JOptionPane.showInputDialog(parent, 
						 "Remote Host", 
						 value);
        return ret;
    }

    /** Make constructor unavailable; class is for namespace only. */
    private RemoteProgramDialog()
    {
    }
}


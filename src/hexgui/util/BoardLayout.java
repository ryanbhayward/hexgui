//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.util;

import javax.swing.*;          
import java.awt.*;
import java.awt.event.*;

public class BoardLayout
    implements LayoutManager
{
    public void addLayoutComponent(String name, Component comp)
    {

    }

    public void layoutContainer(Container parent)
    {
	if (parent.getComponentCount() != 1) {
	    System.err.println("BoardLayout: needs exactly one component!");
	}
	Dimension size = parent.getSize();
        Insets insets = parent.getInsets();
        size.width -= insets.left + insets.right;
        size.height -= insets.top + insets.bottom;
	int w = size.width;
	int h = size.height;
	//if (h*3/2 > w) h = 2*w/3;
	//if (h*3/2 < w) w = h*3/2;

        int x = (size.width - w) / 2;
        int y = (size.height - h) / 2;
        parent.getComponent(0).setBounds(x + insets.left, y + insets.top, w, h);
    }

    public Dimension minimumLayoutSize(Container parent)
    {
	return parent.getComponent(0).getMinimumSize();
    }
    public Dimension preferredLayoutSize(Container parent)
    {
	return parent.getComponent(0).getPreferredSize();
    }

    public void removeLayoutComponent(Component comp) 
    {

    }

}

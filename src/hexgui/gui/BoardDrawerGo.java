//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.gui;

import hexgui.hex.HexPoint;

import javax.swing.*;          
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

//----------------------------------------------------------------------------

public class BoardDrawerGo extends BoardDrawerBase
{
    public BoardDrawerGo()
    {
	super();
	loadBackground("hexgui/images/wood.png");
    }

    protected Point getLocation(int x, int y)
    {
	Point ret = new Point();
	ret.x = m_marginX + (x+2)*m_fieldWidth;
	ret.y = m_marginY + (y+2)*m_fieldHeight;
	return ret;
    }

    // FIXME: center stones on even length sides!!
    protected Point getLocation(HexPoint pos)
    {
	if (pos == HexPoint.EAST) {
	    return getLocation(m_bwidth+1, m_bheight/2);
	} else if (pos == HexPoint.WEST) {
	    return getLocation(-2, m_bheight/2);
	} else if (pos == HexPoint.SOUTH) {
	    return getLocation(m_bwidth/2, m_bheight+1);
	} else if (pos == HexPoint.NORTH) {
	    return getLocation(m_bwidth/2, -2);
	}
	return getLocation(pos.x, pos.y);
    }

    protected int calcFieldWidth(int w, int h, int bw, int bh)
    {
	return w / (bw + 4);
    }

    protected int calcFieldHeight(int w, int h, int bw, int bh)
    {
	return h / (bh + 4);
    }

    // FIXME: not needed... something is wrong with the api?
    protected int calcStepSize()
    {
	return 0;
    }

    protected int calcBoardWidth()
    {
	return (m_bwidth+4)*m_fieldWidth;
    }

    protected int calcBoardHeight()
    {
	return (m_bheight+4)*m_fieldHeight;
    }

    protected Polygon[] calcCellOutlines(GuiField field[])
    {
	int w = m_fieldWidth/2;
	int h = m_fieldHeight/2;
	Polygon outline[] = new Polygon[field.length];
	for (int i=0; i<field.length; i++) {
	    Point c = getLocation(field[i].getPoint());
	    outline[i] = new Polygon();
	    outline[i].addPoint(c.x - w, c.y-h);
	    outline[i].addPoint(c.x + w, c.y-h);
	    outline[i].addPoint(c.x + w, c.y+h);
	    outline[i].addPoint(c.x - w, c.y+h);
	}
	return outline;
    }

    protected void drawCells(Graphics g, GuiField field[])
    {
	g.setColor(Color.black);
	Point p = getLocation(0,0);
	int x = p.x;
	int y = p.y;
	for (int i=0; i<m_bheight; i++) {
	    g.drawLine(x, 
		       y+i*m_fieldHeight, 
		       x+(m_bwidth-1)*m_fieldWidth, 
		       y+i*m_fieldHeight);
	}
	for (int i=0; i<m_bwidth; i++) {
	    g.drawLine(x + i*m_fieldWidth, 
		       y,
		       x + i*m_fieldWidth, 
		       y+(m_bheight-1)*m_fieldHeight);
	}
	// diagonal lines from left edge
	for (int i=1; i<m_bheight; i++) {
	    int j = Math.min(i, m_bwidth-1);
	    g.drawLine(x, 
		       y + i*m_fieldHeight,
		       x + j*m_fieldWidth,
		       y + (i-j)*m_fieldHeight);
	}
	// diagonal lines from bottom edge
	for (int i=1; i<m_bwidth; i++) {
	    int j = Math.min(m_bwidth-i-1, m_bheight-1);
	    int k = Math.max(0, (i+m_bheight-1) - (m_bwidth-1));
	    g.drawLine(x + i*m_fieldWidth,
		       y + (m_bheight-1)*m_fieldHeight,
		       x + (i+j)*m_fieldWidth,
		       y + k*m_fieldHeight);
	}
    }

    protected void drawLabels(Graphics g, boolean alphatop)
    {
	String string;
	int xoffset,yoffset;
	g.setColor(Color.black);

	xoffset = yoffset = 0;
	for (int x=0; x<m_bwidth; x++) {
	    if (alphatop)
		string = Character.toString((char)((int)'A' + x));
	    else
		string = Integer.toString(x+1);
	    drawLabel(g, getLocation(x, -1), string, xoffset);
	    drawLabel(g, getLocation(x, m_bheight), string, xoffset);
	}
	xoffset = yoffset = 0;
	for (int y=0; y<m_bheight; y++) {
	    if (!alphatop)
		string = Character.toString((char)((int)'A' + y));
	    else
		string = Integer.toString(y+1);
	    drawLabel(g, getLocation(-1, y), string, xoffset);
	    drawLabel(g, getLocation(m_bwidth, y), string, xoffset);
	}
    }
}

//----------------------------------------------------------------------------

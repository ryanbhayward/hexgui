//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.gui;

import hexgui.hex.*;
import hexgui.util.*;
import hexgui.game.Node;

import java.util.Vector;
import java.math.BigInteger;
import javax.swing.*;          
import javax.swing.border.EtchedBorder;
import java.awt.print.Printable;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.*;
import java.awt.event.*;

//----------------------------------------------------------------------------

/** Gui Board. */
public final class GuiBoard
    extends JPanel implements Printable
{
    /** Callback for clicks on a field. */
    public interface Listener
    {
	void fieldClicked(HexPoint point, boolean ctrl, boolean shift);
        void fieldDoubleClicked(HexPoint point, boolean ctrl, boolean shift);
    }

    private static final boolean DEFAULT_FLIPPED = true;
    
    public static final int HEXBOARD = 0;
    public static final int YBOARD = 1;

    /** Constructor. */
    public GuiBoard(Listener listener, GuiPreferences preferences)
    {
	m_image = null;
	m_listener = listener;
	m_preferences = preferences;
        m_arrows = new Vector<Pair<HexPoint, HexPoint>>();

	initSize(HEXBOARD, 
                 m_preferences.getInt("gui-board-width"),
		 m_preferences.getInt("gui-board-height"));

	setDrawType(m_preferences.get("gui-board-type"));   

	setPreferredSize(new Dimension
			 (m_preferences.getInt("gui-board-pixel-width"),
	  	          m_preferences.getInt("gui-board-pixel-height")));

        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	setLayout(new BoardLayout());
	m_boardPanel = new BoardPanel();
	add(m_boardPanel);

	MouseAdapter mouseAdapter = new MouseAdapter()
	{
	    public void mouseClicked(MouseEvent e)
	    {
		GuiField f = m_drawer.getFieldContaining(e.getPoint(), m_field);
		if (f == null) return;

                int modifiers = e.getModifiers();
                boolean ctrl = (modifiers & ActionEvent.CTRL_MASK) != 0;
                boolean shift = (modifiers & ActionEvent.SHIFT_MASK) != 0;
                if (e.getClickCount() >= 2)
                    m_listener.fieldDoubleClicked(f.getPoint(), ctrl, shift);
                else
                    m_listener.fieldClicked(f.getPoint(), ctrl, shift);
	    }
	};
	m_boardPanel.addMouseListener(mouseAdapter);

	setVisible(true);
    }

    /** Sets the type of board drawer to use.  If <code>name</code> is
	not one of the values because "Diamond" is is used.
	@param name one of ("Diamond", "Flat", "Go"). 
    */
    public void setDrawType(String name)
    {
        if (name.equals("Y")) {
            m_drawer = new BoardDrawerY();
            initSize(YBOARD, m_width, m_height);
        } else if (name.equals("Go")) {
            if (m_mode != HEXBOARD)
                initSize(HEXBOARD, m_width, m_height);
	    m_drawer = new BoardDrawerGo();
	    m_preferences.put("gui-board-type", "Go");
	} else if (name.equals("Diamond")) {
            if (m_mode != HEXBOARD)
                initSize(HEXBOARD, m_width, m_height);
	    m_drawer = new BoardDrawerDiamond();
	    m_preferences.put("gui-board-type", "Diamond");
	} else if (name.equals("Flat")) {
            if (m_mode != HEXBOARD)
                initSize(HEXBOARD, m_width, m_height);
	    m_drawer = new BoardDrawerFlat();
	    m_preferences.put("gui-board-type", "Flat");
	} else {
	    System.out.println("GuiBoard: unknown draw type '" + name + "'.");
	    m_drawer = new BoardDrawerDiamond();
	} 
        repaint();
    }

    /** Sets whether black on letters is on top or if white on 
	numbers is on top.  If string is invalid defaults to black on top.
	@param orient either "Black on top" or "White on top". 
    */
    public void setOrientation(String orient)
    {
	if (orient.equals("Black on top"))
	    m_preferences.put("gui-board-on-top", "black");
	else if (orient.equals("White on top"))
	    m_preferences.put("gui-board-on-top", "white");
	else {
	    System.out.println("GuiBoard: unknown orientation '" + 
			       orient + "'.");
	}
        repaint();
    }

    public void initSize(int w, int h)
    {
        initSize(m_mode, w, h);
    }

    /** Creates a board of the given dimensions.
        Dirty flag is set to false. 
        @param m type of board to create (HEX or Y)
	@param w width of the board in cells
	@param h height of the board in cells
    */
    private void initSize(int m, int w, int h)
    {
	System.out.println("GuiBoard.initSize: " 
                           + (m == HEXBOARD ? "(HEX) " : "(Y) ") 
                           + w + " " + h);

        m_mode = m;
	m_width = w; 
	m_height = h;
	m_size = new Dimension(m_width, m_height);
        
        m_dirty_stones = false;
        clearArrows();

        if (m_mode == HEXBOARD) 
        {
            m_field = new GuiField[w*h+4];
            for (int x=0; x<w*h; x++) {
                m_field[x] = new GuiField(HexPoint.get(x % w, x / w));
                m_field[x].setAttributes(GuiField.DRAW_CELL_OUTLINE);
            }
            m_field[w*h+0] = new GuiField(HexPoint.NORTH);
            m_field[w*h+1] = new GuiField(HexPoint.SOUTH);
            m_field[w*h+2] = new GuiField(HexPoint.WEST);
            m_field[w*h+3] = new GuiField(HexPoint.EAST);
        } 
        else 
        {
            int n = w*(w+1)/2;
            m_field = new GuiField[n+3];
            for (int y=0,i=0; y<w; y++) {
                for (int x=0; x<=y; x++,i++) {
                    m_field[i] = new GuiField(HexPoint.get(x, y));
                    m_field[i].setAttributes(GuiField.DRAW_CELL_OUTLINE);
                }
            }
            m_field[n+0] = new GuiField(HexPoint.SOUTH);
            m_field[n+0].setAttributes(GuiField.DRAW_CELL_OUTLINE);
            m_field[n+1] = new GuiField(HexPoint.WEST);
            m_field[n+1].setAttributes(GuiField.DRAW_CELL_OUTLINE);
            m_field[n+2] = new GuiField(HexPoint.EAST);
            m_field[n+2].setAttributes(GuiField.DRAW_CELL_OUTLINE);
        }
	clearAll();
        repaint();
    }

    /** Creates a board with the given dimensions.
	Convience function.  
	@param dim dimension of the board
	@see initSize(int, int)
    */
    public void initSize(Dimension dim)
    {
	initSize(m_mode, dim.width, dim.height);
    }

    /** Gets the size of the board.
	@return size of the board as a Dimension.
    */
    public Dimension getBoardSize()
    {
	return m_size;
    }

    public boolean isHexBoard()
    {
        return m_mode == HEXBOARD;
    }

    public boolean isYBoard() 
    {
        return m_mode == YBOARD;
    }

    /** Clears all marks and stones from the board. */
    public void clearAll()
    {
	for (int x=0; x<m_field.length; x++)
	    m_field[x].clear();
        if (m_mode == HEXBOARD) 
        {
            getField(HexPoint.NORTH).setColor(HexColor.BLACK);
            getField(HexPoint.SOUTH).setColor(HexColor.BLACK);
            getField(HexPoint.WEST).setColor(HexColor.WHITE);
            getField(HexPoint.EAST).setColor(HexColor.WHITE);
        } 
        else 
        {
            getField(HexPoint.SOUTH).setColor(HexColor.EMPTY);
            getField(HexPoint.WEST).setColor(HexColor.EMPTY);
            getField(HexPoint.EAST).setColor(HexColor.EMPTY);
        }
        repaint();
    }

    /** Makes a copy of the current fields if the dirty flag is not already set,
        and then sets the dirty flag to true. See clearMarks().
    */
    public void aboutToDirtyStones()
    {
        if (!m_dirty_stones) {
            m_backup_field = new GuiField[m_field.length];
            for (int i=0; i<m_field.length; i++) 
                m_backup_field[i] = new GuiField(m_field[i]);
        }
        m_dirty_stones = true;
    }

    public boolean areStonesDirty()
    {
        return m_dirty_stones;
    }

    /** Adds an arrow. */
    public void addArrow(HexPoint from, HexPoint to)
    {
        m_arrows.add(new Pair<HexPoint, HexPoint>(from, to));
        repaint();
    }

    public void clearArrows()
    {
        m_arrows.clear();
        repaint();
    }

    /** Clears dynamnic marks, leaving stones intact. If the dirty flag is set,
        revert the fields to the saved fields saved in markStonesDirty().
        Dirty stones flag is set to false. See aboutToDirtyStones().
        Empties the list of arrows. 
     */
    public void clearMarks()
    {
        if (m_dirty_stones) {
            for (int i=0; i<m_field.length; i++) 
                m_field[i] = new GuiField(m_backup_field[i]);
        }
        m_dirty_stones = false;
        
        clearArrows();
        
	for (int x=0; x<m_field.length; x++) 
	    m_field[x].clearAttributes(GuiField.LAST_PLAYED | 
                                       GuiField.SWAP_PLAYED | 
                                       GuiField.DRAW_TEXT | 
                                       GuiField.DRAW_ALPHA);
        repaint();
    }

    /** Sets the given point to the given color.
        Special points are ignored (SWAP_PIECES, RESIGN, etc).
	@param point the point
	@param color the color to set it to.
    */
    public void setColor(HexPoint point, HexColor color)
    {
        if (HexPoint.SWAP_PIECES == point || HexPoint.RESIGN == point) {
            return;
        }

	GuiField f = getField(point);
	f.setColor(color);
        repaint();
    }

    /** Gets the color of the specified point.
	@param point the point whose color we with to obtain.
	@return the color of <code>point</code>
    */
    public HexColor getColor(HexPoint point)
    {
	GuiField f = getField(point);
	return f.getColor();
    }

    /** Gets the field at the specified point. 
        Special points are ignored (SWAP_PIECES, etc).
    */
    public GuiField getField(HexPoint point)
    {
        if (HexPoint.SWAP_PIECES == point || HexPoint.RESIGN == point) {
            return null;
        }
        
	for (int x=0; x<m_field.length; x++) {
	    if (m_field[x].getPoint() == point) 
		return m_field[x];
        }
	assert(false);
	return null;
    }

    /** Marks the given point to show which move was played last, or
        clears the mark if <code>point</code> is <code>null</code>. */

    // FIXME: don't store the last played move here, just get it from
    //        the game tree. 

    public void markLastPlayed(HexPoint point)
    {
        assert(point != HexPoint.SWAP_PIECES);

	if (m_last_played != null) 
	    m_last_played.clearAttributes(GuiField.LAST_PLAYED);
	if (point != null) {
	    m_last_played = getField(point);
	    m_last_played.setAttributes(GuiField.LAST_PLAYED);
	} else {
	    m_last_played = null;
	}
        repaint();
    }

    /** Marks the given point to show which move was swapped. */
    public void markSwapPlayed(HexPoint point)
    {
        if (point == null) {
            if (m_swap_played != null) {
                m_swap_played.clearAttributes(GuiField.SWAP_PLAYED);
                m_swap_played = null;
            }
        } else {
            m_swap_played = getField(point);
            m_swap_played.setAttributes(GuiField.SWAP_PLAYED);
        }
        repaint();
    }

    /** Sets the given points's alpha color. */
    public void setAlphaColor(HexPoint point, Color color)
    {
        if (point == HexPoint.get("swap-pieces"))
            return;
        if (point == HexPoint.get("resign"))
            return;
	getField(point).setAlphaColor(color);
        repaint();
    }

    public void setAlphaColor(HexPoint point, Color color, float blend)
    {
        if (point == HexPoint.get("swap-pieces"))
            return;
        if (point == HexPoint.get("resign"))
            return;
	getField(point).setAlphaColor(color, blend);
        repaint();
    }

    /** Returns the points's alpha color; null if it is 'swap-pieces'
        or resign. */
    public Color getAlphaColor(HexPoint point)
    {
        if (point == HexPoint.get("swap-pieces"))
            return null;
        if (point == HexPoint.get("resign"))
            return null;

	return getField(point).getAlphaColor();
    }
    
    /** Sets the given points's text. */
    public void setText(HexPoint point, String str)
    {
        getField(point).setText(str);
        repaint();
    }

    /** Sets wheither this cell is selected. */
    public void setSelected(HexPoint point, boolean selected)
    {
        getField(point).setSelected(selected);
        repaint();
    }

    public boolean isBoardFull()
    {
        for (int x=0; x<m_field.length; x++) {
            if (m_field[x].getColor() == HexColor.EMPTY)
                return false;
        }
        return true;
    }

    public void swapColors() 
    {
        for (int x=0; x<m_field.length; x++) {
            HexPoint point = m_field[x].getPoint();
            if (point == HexPoint.NORTH || point == HexPoint.EAST ||
                point == HexPoint.SOUTH || point == HexPoint.WEST)
                continue;
            HexColor color = m_field[x].getColor();
            m_field[x].setColor(color.otherColor());
        }
    }

    /** Stores the current state as a setup position in the
        given sgf node. */
    public void storePosition(Node node)
    {
        for (int x=0; x<m_field.length; x++) {
            HexPoint point = m_field[x].getPoint();
            if (point == HexPoint.NORTH || point == HexPoint.EAST ||
                point == HexPoint.SOUTH || point == HexPoint.WEST)
                continue;
                
            HexColor color = m_field[x].getColor();
            if (color == HexColor.EMPTY)
                continue;

            node.addSetup(color, point);
        }
    }

    public void paintImmediately()
    {
        assert SwingUtilities.isEventDispatchThread();
	super.paintImmediately(0, 0, getWidth(),getHeight());
    }

    /** Displays this vc on the board. */
    public void displayVC(VC vc)
    {
        getField(vc.getFrom()).setAlphaColor(Color.blue);
        getField(vc.getTo()).setAlphaColor(Color.blue);
        
        Vector<HexPoint> carrier = vc.getCarrier();
        for (int i=0; i<carrier.size(); i++) 
            getField(carrier.get(i)).setAlphaColor(Color.green);

        Vector<HexPoint> stones = vc.getStones();
        for (int i=0; i<stones.size(); i++) 
            getField(stones.get(i)).setAlphaColor(Color.red);

        Vector<HexPoint> key = vc.getKey();
        for (int i=0; i<key.size(); i++)
            getField(key.get(i)).setAlphaColor(Color.yellow);
    }

    //------------------------------------------------------------

    public int print(Graphics g, PageFormat format, int page)
        throws PrinterException
    {
        if (page >= 1)
        {
            return Printable.NO_SUCH_PAGE;
        }
        double width = getWidth();
        double height = getHeight();
        double pageWidth = format.getImageableWidth();
        double pageHeight = format.getImageableHeight();
        double scale = 1;
        if (width >= pageWidth)
            scale = pageWidth / width;
        double xSpace = (pageWidth - width * scale) / 2;
        double ySpace = (pageHeight - height * scale) / 2;
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(format.getImageableX() + xSpace,
                      format.getImageableY() + ySpace);
        g2d.scale(scale, scale);
        print(g2d);
        return Printable.PAGE_EXISTS;
    }

    //------------------------------------------------------------

    /** Converts a hex string representing a bitset into a vector of
        HexPoints.  This relies on m_field being ordered in a particular
        fashion.
        
        NOTE: THIS IS BROKEN SINCE HexPoint was changed in wolve, r182. 
              USE BASE 64 INSTEAD!

        FIXME: switch carriers to be printed as a list of HexPoints instead of 
        as hex strings?
    */
    private Vector<HexPoint> convertHexString(String str)
    {
        Vector<HexPoint> ret = new Vector<HexPoint>();

        for (int i=0; i<str.length(); i++) {
            BigInteger big = new BigInteger(StringUtils.reverse(str), 16);
            for (int j=0; j<m_field.length; j++) {
                if (big.testBit(j))
                    ret.add(m_field[j].getPoint());
            }
        }

        return ret;
    }


    /** Converts a base 64 string representing a bitset into a vector of
        HexPoints.  
    */
    private static final String m_base64 
      = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+/";

    private Vector<HexPoint> convertBase64String(String str)
    {

        Vector<HexPoint> ret = new Vector<HexPoint>();
        for (int i=0; i<str.length(); i++) {
            int v = m_base64.indexOf(str.charAt(i));
            assert(v != -1);
            for (int j=0; j<6 && i*6 + j < HexPoint.MAX_POINTS; j++) {
                if ((v & (1 << j)) != 0)
                    ret.add(HexPoint.get(i*6 + j));
            }
        }
        return ret;
    }

    private GuiField[] flipFields(GuiField field[])
    {
	GuiField out[] = new GuiField[field.length];
	for (int i=0; i<field.length; i++) {
	    HexPoint p = field[i].getPoint();
	    out[i] = new GuiField(field[i]);
	    if (p == HexPoint.NORTH)
		out[i].setPoint(HexPoint.WEST);
	    else if (p == HexPoint.WEST)
		out[i].setPoint(HexPoint.NORTH);
	    else if (p == HexPoint.EAST)
		out[i].setPoint(HexPoint.SOUTH);
	    else if (p == HexPoint.SOUTH)
		out[i].setPoint(HexPoint.EAST);
	    else {
		out[i].setPoint(HexPoint.get(p.y, p.x));
	    }	    
	}
	return out;
    }

    private class BoardPanel
	extends JPanel
    {
	public BoardPanel()
	{
	    setFocusable(true);
	}

	public void paintComponent(Graphics graphics)
	{
	    int w = getWidth();
	    int h = getHeight();

	    if (m_image == null) {
		m_image = createImage(w, h);
	    }

	    int bw = m_width;
	    int bh = m_height;
	    GuiField ff[] = m_field;
	    boolean alphaontop = true;
            Vector<Pair<HexPoint, HexPoint>> arrows = m_arrows;

	    if (m_preferences.get("gui-board-on-top").equals("white")) {
		bw = m_height;
		bh = m_width;
		alphaontop = false;
		ff = flipFields(m_field);

                arrows = new Vector<Pair<HexPoint, HexPoint>>();
                for (int i=0; i<m_arrows.size(); i++) {
                    HexPoint p1 = m_arrows.get(i).first;
                    HexPoint p2 = m_arrows.get(i).second;
                    arrows.add(new Pair<HexPoint, HexPoint>
                               (HexPoint.get(p1.y, p1.x),
                                HexPoint.get(p2.y, p2.x)));
                }
	    }

	    m_drawer.draw(m_image.getGraphics(), 
                          w, h, bw, bh, alphaontop, 
                          ff, arrows);
	    graphics.drawImage(m_image, 0, 0, null);
	}

	public void setBounds(int x, int y, int w, int h)
	{
	    super.setBounds(x, y, w, h);
	    m_image = null;
	}
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    private int m_width, m_height;
    private Dimension m_size;
    private int m_mode;

    private Image m_image;
    private GuiField m_field[];
    private Vector<Pair<HexPoint, HexPoint>> m_arrows;

    private boolean m_dirty_stones;
    private GuiField m_backup_field[];

    private GuiField m_last_played;
    private GuiField m_swap_played;

    private BoardDrawerBase m_drawer;
    private BoardPanel m_boardPanel;

    private Listener m_listener;
    private GuiPreferences m_preferences;
}

//----------------------------------------------------------------------------

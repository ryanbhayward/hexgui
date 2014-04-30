package hexgui.hex;

import java.lang.Exception;
import java.lang.NumberFormatException;
import java.awt.Dimension;

/** A cell on a Hex board. 
    In addition to each playable cell, HexPoints are created for each edge of 
    the board and for some special cases like swap moves, resignations, and
    forfeitures. */
public final class HexPoint implements Comparable
{
    /**  Exception. */
    public static class InvalidHexPointException
	extends Exception
    {
	public InvalidHexPointException(String message)
	{
	    super("Invalid point: " + message);
	}
    }

    public static final HexPoint NORTH;
    public static final HexPoint SOUTH;
    public static final HexPoint WEST;
    public static final HexPoint EAST;
    public static final HexPoint SWAP_PIECES;
    public static final HexPoint RESIGN;
    public static final HexPoint INVALID;

    public static final int MAX_WIDTH  = 19;
    public static final int MAX_HEIGHT = 19;
    public static final int MAX_POINTS = MAX_WIDTH*MAX_HEIGHT + 7;

    public static final int DEFAULT_SIZE = 11;

    private static HexPoint s_points[];

    static 
    {
	s_points = new HexPoint[MAX_POINTS];

        INVALID     = s_points[0] = new HexPoint(0, "invalid");
	RESIGN      = s_points[1] = new HexPoint(1, "resign");
	SWAP_PIECES = s_points[2] = new HexPoint(2, "swap-pieces");

	NORTH       = s_points[3] = new HexPoint(3, "north");
	EAST        = s_points[4] = new HexPoint(4, "east");
	SOUTH       = s_points[5] = new HexPoint(5, "south");
	WEST        = s_points[6] = new HexPoint(6, "west");

	for (int y=0; y<MAX_HEIGHT; y++) {
	    for (int x=0; x<MAX_WIDTH; x++) {
		String name = "" + (char)('a' + x) + (y+1);
		s_points[7 + y*MAX_WIDTH+ x] = new HexPoint(x, y, name);
	    }
	}
    }

    /** Returns the point with the given index.

	@param i index of the point. 
	@return point with index i.
    */
    public static HexPoint get(int i)
    {
	assert(i >= 0);
	assert(i < MAX_POINTS);
	return s_points[i];
    }

    /** Returns the point with the given coordinates.  Note that it is
	not possible to obtain points for board edges and special
	moves with this method.  Use the <code>get(String)</code>
	method for these types of points.

	@param x x-coordinate of point
	@param y y-coordinate of point
	@return point with coordinates (x,y). 
    */
    public static HexPoint get(int x, int y)
    {
	assert(x >= 0);
	assert(y >= 0);
	assert(x < MAX_WIDTH);
	assert(y < MAX_HEIGHT);
	return s_points[7 + y*MAX_WIDTH + x];
    }
    
    /** Returns the point with the given string represention.
	Valid special moves include: "north", "south", "east", "west" 
	"swap-sides", "swap-pieces", "resign", and "forfeit". 
	@param name The name of the point to return
	@return the point or <code>null</code> if <code>name</code> is invalid.
    */
    public static HexPoint get(String name) 
    {
        if (name.equalsIgnoreCase("swap"))
            return SWAP_PIECES;

        for (int x=0; x<MAX_POINTS; x++) 
            if (name.equalsIgnoreCase(s_points[x].toString()))
                return s_points[x];
        assert(false);
	return null;
    }

    /** Returns the string representation of the point. */
    public String toString()
    {
	return m_string;
    }

    /** Convert a list of points to a string.
        Points are separated by a single space.
        If pointList is null, "(null)" is returned. */
    public static String toString(ConstPointList pointList)
    {
        if (pointList == null)
            return "(null)";
        int length = pointList.size();
        StringBuilder buffer = new StringBuilder(length * 4);
        for (int i = 0; i < length; ++i)
        {
            buffer.append(pointList.get(i));
            if (i < length - 1)
                buffer.append(' ');
        }
        return buffer.toString();
    }

    public int compareTo(Object other)
    {
        if (other instanceof HexPoint) {
            HexPoint o = (HexPoint)other;
            if (this.x == o.x && this.y == o.y) return 0;
            if (this.x != o.x) return this.x - o.x;
            return this.y - o.y;
        }
        return -1;
    }

    private HexPoint(int p, String name)
    {
        this.x = p & (MAX_WIDTH-1);
        this.y = p / MAX_WIDTH;
        m_string = name;
    }

    private HexPoint(int x, int y, String name)
    {
	this.x = x;
	this.y = y;
	m_string = name;
    }

    public final int x, y;
    private final String m_string;
}

//----------------------------------------------------------------------------

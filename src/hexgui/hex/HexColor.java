package hexgui.hex;

/** Possible states of a cell on a Hex board (black, white, or empty). */
public final class HexColor
{
    public static final HexColor EMPTY;
    public static final HexColor WHITE;
    public static final HexColor BLACK;

    /** Returns a string representation of this color.
	@return "black", "white", or "empty".
    */
    public String toString()
    {
	return m_string;
    }

    /** Returns the other opposite color.
	@return WHITE for BLACK, BLACK for WHITE, EMPTY otherwise. 
    */
    public HexColor otherColor()
    {
	return m_otherColor;
    }

    public static HexColor get(String name)
    {
        if (name.equals("black")) return BLACK;
        if (name.equals("white")) return WHITE;
        if (name.equals("empty")) return EMPTY;
        return null;
    }

    private final String m_string;
    private HexColor m_otherColor;

    static 
    {
	BLACK = new HexColor("black");
	WHITE = new HexColor("white");
	EMPTY = new HexColor("empty");
	BLACK.setOtherColor(WHITE);
	WHITE.setOtherColor(BLACK);
	EMPTY.setOtherColor(EMPTY);
    }

    private HexColor(String string)
    {
	m_string = string;
    }
    private void setOtherColor(HexColor color)
    {
	m_otherColor = color;
    }
}

//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.hex;

import java.util.Vector;

//----------------------------------------------------------------------------

/** 
    VC.
    A connection between two cells. 
*/
public class VC
{
    /** Constructs a VC. */
    public VC(HexPoint from, HexPoint to, HexColor c, String type)
    {
        this(from, to, c, type, "unknown", 0, 
             new Vector<HexPoint>(), 
             new Vector<HexPoint>(), 
             new Vector<HexPoint>());
    }

    public VC(HexPoint from, HexPoint to, 
              HexColor c, String type,
              String source, int moves, 
              Vector<HexPoint> carrier, 
              Vector<HexPoint> stones, 
              Vector<HexPoint> key)
    {
        m_from = from;
        m_to = to;
        m_color = c;
        m_type = type;
        m_source = source;
        m_moves = moves;
        m_carrier = carrier;
        m_stones = stones;
        m_key = key;
    }

    public String toString()
    {
        StringBuilder ret = new StringBuilder();

        if (m_type.equals("softlimit")) {
            return "---------------- softlimit ----------------";
        }

        ret.append(m_from.toString());
        ret.append(" ");
        ret.append(m_to.toString());
        ret.append(" ");
        ret.append(m_color.toString());
        ret.append(" ");
        ret.append(m_type);
        ret.append(" ");
        ret.append(m_source);
        ret.append(" ");
        ret.append(Integer.toString(m_moves));
        ret.append(" ");

        ret.append("[");
        for (int i=0; i<m_carrier.size(); i++) {
            ret.append(" ");
            ret.append(m_carrier.get(i).toString());
        }
        ret.append(" ] ");

        ret.append("[");
        for (int i=0; i<m_stones.size(); i++) {
            ret.append(" ");
            ret.append(m_stones.get(i).toString());
        }
        ret.append(" ] ");

        for (int i=0; i<m_key.size(); i++) {
            ret.append(" ");
            ret.append(m_key.get(i).toString());
        }
 
        return ret.toString();
    }

    public HexPoint getFrom()  { return m_from; }
    public HexPoint getTo()    { return m_to; }
    public HexColor getColor() { return m_color; }
    public String getType() { return m_type; }
    public Vector<HexPoint> getCarrier() { return m_carrier; }
    public Vector<HexPoint> getStones() { return m_stones; }
    public Vector<HexPoint> getKey() { return m_key; }
    public String getSource() { return m_source; }

    private HexPoint m_from;
    private HexPoint m_to;
    private HexColor m_color;
    private String m_type;
    private int m_moves;
    private Vector<HexPoint> m_carrier;
    private Vector<HexPoint> m_stones;
    private Vector<HexPoint> m_key;
    private String m_source;
}

//----------------------------------------------------------------------------

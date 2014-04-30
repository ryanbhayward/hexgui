//----------------------------------------------------------------------------
// $Id$ 
//----------------------------------------------------------------------------

package hexgui.game;

import hexgui.hex.HexColor;
import hexgui.hex.HexPoint;
import hexgui.hex.Move;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

//----------------------------------------------------------------------------

/** Node in a game tree.
    Stores moves and other properties.
*/
public class Node
{
    /** Initializes an empty node with a null move. */
    public Node()
    {
	this(null);
    }

    /** Constructs a new node with the specified move.
	@param move move to initialize the node with.
    */
    public Node(Move move)
    {
	m_property = new TreeMap<String, String>();
        m_setup_black = new Vector<HexPoint>();
        m_setup_white = new Vector<HexPoint>();
        m_setup_empty = new Vector<HexPoint>();
        m_label = new Vector<String>();
	setMove(move);
    }

    public void setMove(Move move) { m_move = move;  }
    public Move getMove() { return m_move; }
    public boolean hasMove() { return m_move != null; }

    public void setParent(Node parent) { m_parent = parent; }
    public Node getParent() { return m_parent; }

    public void setPrev(Node prev) { m_prev = prev; }
    public Node getPrev() { return m_prev; }

    public void setNext(Node next) { m_next = next; }
    public Node getNext() { return m_next; }

    /** Sets the first child of this node.  
	This does not update the sibling pointers of the child.
    */
    public void setFirstChild(Node child) { 
	m_child = child; 
    }

    /** Removes this node from the gametree. */
    public void removeSelf()
    {
	Node prev = getPrev();
	Node next = getNext();

        if (prev == null) { 
            // need to fix parent since we're first child
	    if (getParent() != null) getParent().setFirstChild(next);
	} else {
	    prev.setNext(next);
        }

        if (next != null) next.setPrev(prev);
    }

    /** Adds a child to the end of the list of children. 
        @param child Node to be added to end of list.
    */     
    public void addChild(Node child) 
    {
	child.setNext(null);
	child.setParent(this);
        
	if (m_child == null) {
	    m_child = child;
	    child.setPrev(null);
	} else {
	    Node cur = m_child;
	    while (cur.getNext() != null)
		cur = cur.getNext();
	    cur.setNext(child);
	    child.setPrev(cur);
	}
    }
    
    /** Returns the number of children of this node. */
    public int numChildren()
    {
	int num = 0;
	Node cur = m_child;
	while (cur != null) {
	    num++;
	    cur = cur.getNext();
	}	
	return num;
    }

    /** Returns the nth child. 
	@param n The number of the child to return. 
	@return  The nth child or <code>null</code> that child does not exist.
    */
    public Node getChild(int n) 
    {
	Node cur = m_child;
	for (int i=0; cur != null; i++) {
	    if (i == n) return cur;
	    cur = cur.getNext();
	}
	return null;
    }

    /** Returns the first child. 
	@return first child or <code>null</code> if no children.
    */
    public Node getChild() { return getChild(0); }

    /** Returns the child that contains <code>node</code> in its subtree. */
    public Node getChildContainingNode(Node node)
    {
	for (int i=0; i<numChildren(); i++) {
	    Node c = getChild(i);
	    if (c == node) return c;
	}
	for (int i=0; i<numChildren(); i++) {
	    Node c = getChild(i);
	    if (c.getChildContainingNode(node) != null)
		return c;
	}
	return null;
    }

    /** Returns the depth of this node.
     */
    public int getDepth()
    {
        Node cur;
        int depth = 0;
        for (cur = this; ; depth++) {
            Node parent = cur.getParent();
            if (parent == null) break;
            cur = parent;
        }
        return depth;
    }

    /** Determines if a swap move is allowed at this node.
        Returns <code>true</code> if we are on move #2. 
    */
    public boolean isSwapAllowed()
    {
        if (getDepth() == 1) return true;
        return false;
    }

    //----------------------------------------------------------------------

    /** Adds a property to this node. 
	Node properties are <code>(key, value)</code> pairs of strings.
	These properties will stored if the gametree is saved in SGF format.
	@param key name of the property
	@param value value of the property
    */
    public void setSgfProperty(String key, String value)
    {
	m_property.put(key, value);
    }

    public void appendSgfProperty(String key, String toadd)
    {
        String old = m_property.get(key);
        if (old == null) old = "";
        m_property.put(key, old+toadd);
    }

    /** Returns the value of a property. 
	@param key name of property
	@return value of <code>key</code> or <code>null</code> if key is
	not in the property list.
    */                
    public String getSgfProperty(String key)
    {
	return m_property.get(key);
    }

    /** Returns a map of the current set of properties.
	@return Map containing the properties
    */
    public Map<String,String> getProperties()
    {
	return m_property;
    }

    /** Sets the SGF Comment field of this node. */
    public void setComment(String comment) { setSgfProperty("C", comment); }
    
    public String getComment() { return getSgfProperty("C"); }

    public boolean hasCount() 
    {
        return (getSgfProperty("CN") != null);
    }

    public String getCount()
    {
        return getSgfProperty("CN");
    }            

    /** Adds a stone of specified color to the setup list and the sgf
        property string. */
    public void addSetup(HexColor color, HexPoint point)
    {
        if (color == HexColor.BLACK) {
            if (!m_setup_black.contains(point)) {
                m_setup_black.add(point);
            }
        } else if (color == HexColor.WHITE) {
            if (!m_setup_white.contains(point)) {
                m_setup_white.add(point);
            }
        } else if (color == HexColor.EMPTY) {
            if (!m_setup_empty.contains(point)) {
                m_setup_empty.add(point);
            }
        } 
    }

    public void removeSetup(HexColor color, HexPoint point)
    {
        if (color == HexColor.BLACK) {
            m_setup_black.remove(point);
        } else if (color == HexColor.WHITE) {
            m_setup_white.remove(point);
        } else if (color == HexColor.EMPTY) {
            m_setup_empty.remove(point);
        }
    }
    
    /** Returns the set of setup stones of color. */
    public Vector<HexPoint> getSetup(HexColor color) 
    {
        if (color == HexColor.BLACK)
            return m_setup_black;
        if (color == HexColor.WHITE)
            return m_setup_white;
        if (color == HexColor.EMPTY)
            return m_setup_empty;
        return null;
    }

    public boolean hasSetup()
    {
        return (!m_setup_black.isEmpty() ||
                !m_setup_white.isEmpty() || 
                !m_setup_empty.isEmpty());
    }
    
    public boolean hasLabel()
    {
        return !m_label.isEmpty();
    }

    public Vector<String> getLabels()
    {
        return m_label;
    }
    
    public void addLabel(String str)
    {
        m_label.add(str);
    }
    
    /** Sets the PL proprety to the given color. */
    public void setPlayerToMove(HexColor color)
    {
        setSgfProperty("PL", (color == HexColor.BLACK) ? "B" : "W");
    }

    /** Returns the color in the "PL" property, null otherwise. */
    public HexColor getPlayerToMove()
    {
        String cstr = getSgfProperty("PL");
        if (cstr != null) {
            if (cstr.equals("B")) return HexColor.BLACK;
            if (cstr.equals("W")) return HexColor.WHITE; 
        }
        return null;
    }

    //----------------------------------------------------------------------

    private TreeMap<String,String> m_property;

    private Vector<HexPoint> m_setup_black;
    private Vector<HexPoint> m_setup_white;
    private Vector<HexPoint> m_setup_empty;

    private Vector<String> m_label;

    private Move m_move;
    private Node m_parent, m_prev, m_next, m_child;
}

//----------------------------------------------------------------------------

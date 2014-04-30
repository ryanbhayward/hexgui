//----------------------------------------------------------------------------
// $Id$ 
//----------------------------------------------------------------------------

package hexgui.game;

import hexgui.hex.Move;
import java.awt.Dimension;

//----------------------------------------------------------------------------

/** Properties of a game.
    Holds properties that appear in a SGF root node.
*/

public class GameInfo
{

    public GameInfo()
    {
    }

    public void setBoardSize(Dimension dim) { m_boardsize = dim; }
    public Dimension getBoardSize() { return m_boardsize; }

    private Dimension m_boardsize;
}

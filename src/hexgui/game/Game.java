//----------------------------------------------------------------------------
// $Id$ 
//----------------------------------------------------------------------------

package hexgui.game;

//----------------------------------------------------------------------------

/** Game state.
    
    A game can be in one of four states: <code>STARTING</code>,
    <code>RUNNING</code>, <code>PAUSED</code>, and
    <code>FINISHED</code>.  The game is <code>STARTING</code> when the
    first move has yet to be played.  Once the first move is played
    then the game is <code>RUNNING</code>.  The clock is ticking only
    when the game is running.  If a side-to-side connection is
    achieved or a player forfeits or resigns then the game is
    <code>FINISHED</code>.  Moves can only be played in the first
    three states.
*/
public final class Game
{

    public static final int STARTING = 0;
    public static final int RUNNING = 1;
    public static final int PAUSED = 2;
    public static final int FINISHED = 3;    

    public Game()
    {
	m_state = STARTING;
    }

    public int getState() { return m_state; }

    private int m_state;

    private Node m_root;
    private Node m_current;
}

//----------------------------------------------------------------------------
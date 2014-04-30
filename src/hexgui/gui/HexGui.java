package hexgui.gui;

import hexgui.hex.*;
import hexgui.util.Pair;
import hexgui.util.StringUtils;
import hexgui.game.Node;
import hexgui.game.GameInfo;
import hexgui.game.Clock;
import hexgui.sgf.SgfWriter;
import hexgui.sgf.SgfReader;
import hexgui.htp.HtpController;
import hexgui.htp.HtpError;
import hexgui.util.StreamCopy;
import hexgui.version.Version;
import hexgui.gui.ParameterDialog;
import hexgui.htp.AnalyzeDefinition;
import hexgui.htp.AnalyzeCommand;
import hexgui.htp.AnalyzeType;
import hexgui.util.ErrorMessage;
import hexgui.gui.ShowAnalyzeText;

import java.io.*;
import static java.text.MessageFormat.format;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.net.*;

//----------------------------------------------------------------------------

/** HexGui. */
public final class HexGui
    extends JFrame
    implements ActionListener, GuiBoard.Listener, 
               HtpShell.Callback, HtpController.GuiFxCallback, 
               AnalyzeDialog.Listener, Comment.Listener
{
    public HexGui(final File file, final String command)
    {
        super("HexGui");
        setIcon();

	System.out.println("HexGui v" + Version.id + "; " + Version.date
			   + "\n");

	// Catch the close action and shutdown nicely
	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	addWindowListener(new java.awt.event.WindowAdapter()
	    {
		public void windowClosing(WindowEvent winEvt) {
		    cmdShutdown();
		}
	    });

        m_selected_cells = new Vector<HexPoint>();

        m_about = new AboutDialog(this);

	m_preferences = new GuiPreferences(getClass());

	m_menubar = new GuiMenuBar(this, m_preferences);
	setJMenuBar(m_menubar.getJMenuBar());

	m_toolbar = new GuiToolBar(this, m_preferences);
        getContentPane().add(m_toolbar.getJToolBar(), BorderLayout.NORTH);

        m_statusbar = new StatusBar();
        getContentPane().add(m_statusbar, BorderLayout.SOUTH);

	m_guiboard = new GuiBoard(this, m_preferences);
        getContentPane().add(m_guiboard, BorderLayout.CENTER);

        m_showAnalyzeText = new ShowAnalyzeText(this, m_guiboard);

        JPanel panel = new JPanel(new BorderLayout());
        getContentPane().add(panel, BorderLayout.EAST);

        m_blackClock = new Clock();
        m_whiteClock = new Clock();
        m_gameinfopanel = new GameInfoPanel(m_blackClock, m_whiteClock);
        m_comment = new Comment(this);
        panel.add(m_gameinfopanel, BorderLayout.NORTH);
        panel.add(m_comment, BorderLayout.CENTER);

	cmdNewGame();

        pack();

        m_locked = false;

        m_semaphore = new Semaphore(1);
        m_htp_queue = new ArrayBlockingQueue<HtpCommand>(256);
        new Thread(new CommandHandler(this, m_htp_queue)).start();

        setVisible(true);
        // After frame is visible, further code using Swing functions must
        // be run in the Swing event dispatch thread.
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    initialize(file, command);
                } });
    }

    //-------------------------------------------------------------------

    public void actionPerformed(ActionEvent e)
    {
	String cmd = e.getActionCommand();

	//
	// system commands
	//
	if (cmd.equals("shutdown"))
	    cmdShutdown();
        else if (cmd.equals("new-program"))
            cmdNewProgram();
        else if (cmd.equals("edit-program"))
            cmdEditProgram();
        else if (cmd.equals("delete-program"))
            cmdDeleteProgram();
	else if (cmd.equals("connect-program"))
	    cmdConnectRemoteProgram();
	else if (cmd.equals("connect-local-program"))
	    cmdConnectLocalProgram();
	else if (cmd.equals("disconnect-program"))
	    cmdDisconnectProgram();
        else if (cmd.equals("reconnect-program"))
            cmdReconnectProgram();
	//
	// file/help commands
	//
	else if (cmd.equals("newgame"))
	    cmdNewGame();
	else if (cmd.equals("savegame"))
        {
	    if (gameChanged())
                cmdSaveGame();
        }
	else if (cmd.equals("savegameas"))
	    cmdSaveGameAs();
	else if (cmd.equals("loadgame"))
	    cmdLoadGame();
        else if (cmd.equals("save-position-as"))
            cmdSavePositionAs();
        else if (cmd.equals("print-preview"))
            cmdPrintPreview();
        else if (cmd.equals("print"))
            cmdPrint();
	else if (cmd.equals("about"))
	    cmdAbout();
	//
	// gui commands
	//
	else if (cmd.equals("gui_toolbar_visible"))
	    cmdGuiToolbarVisible();
	else if (cmd.equals("gui_shell_visible"))
	    cmdGuiShellVisible();
	else if (cmd.equals("gui_analyze_visible"))
            cmdGuiAnalyzeVisible();
	else if (cmd.equals("gui_board_draw_type"))
	    cmdGuiBoardDrawType();
	else if (cmd.equals("gui_board_orientation"))
	    cmdGuiBoardOrientation();
        else if (cmd.equals("show-preferences"))
            cmdShowPreferences();
        else if (cmd.equals("gui-clear-marks"))
            cmdClearMarks();
	//
        // game navigation commands
	//
        else if (cmd.equals("game_beginning"))
	    backward(1000);
	else if (cmd.equals("game_backward10"))
	    backward(10);
	else if (cmd.equals("game_back"))
	    backward(1);
	else if (cmd.equals("game_forward"))
	    forward(1);
	else if (cmd.equals("game_forward10"))
	    forward(10);
	else if (cmd.equals("game_end"))
	    forward(1000);
	else if (cmd.equals("game_up"))
	    up();
	else if (cmd.equals("game_down"))
	    down();
        else if (cmd.equals("game_swap"))
            humanMove(new Move(HexPoint.get("swap-pieces"), m_tomove));
	else if (cmd.equals("genmove"))
	    htpGenMove(m_tomove);
        else if (cmd.equals("game_delete_branch"))
            cmdDeleteBranch();
        else if (cmd.equals("game_start_clock"))
            startClock();
        else if (cmd.equals("game_stop_clock"))
            stopClock();
	else if (cmd.equals("stop"))
            m_white.interrupt();
        else if (cmd.equals("toggle_tomove"))
        {
            cmdToggleToMove();
        }
        else if (cmd.equals("set_to_move"))
        {
            cmdSetToMove();
        }
        //
        // other
        //
        else if (cmd.equals("show_consider_set"))
        {
            Runnable cb = new Runnable() 
                { public void run() { cbShowInferiorCells(); } };
            Runnable callback = new GuiRunnable(cb);            
            sendCommand("vc-build " + m_tomove.toString() + "\n", callback);
        }
        else if (cmd.equals("solve_state"))
        {
            sendCommand("param_dfpn use_guifx 1\n", null);
            sendCommand("dfpn-solve-state " + m_tomove + "\n", null);
        }
        else if (cmd.equals("program_options"))
        {
            AnalyzeCommand command;
            if (m_white_name.equalsIgnoreCase("Mohex"))
            {
                command = new AnalyzeCommand
                    (new AnalyzeDefinition("param/blah/param_mohex"));
                Runnable cb = new Runnable() 
                    { public void run() { cbEditParameters(); } };
                Runnable callback = new GuiRunnable(cb);
                m_curAnalyzeCommand = command;
                sendCommand(command.getCommand() + "\n", callback);
            }
            else if (m_white_name.equalsIgnoreCase("Wolve"))
            {
                command = new AnalyzeCommand
                    (new AnalyzeDefinition("param/blah/param_wolve"));
                Runnable cb = new Runnable() 
                    { public void run() { cbEditParameters(); } };
                Runnable callback = new GuiRunnable(cb);
                m_curAnalyzeCommand = command;
                sendCommand(command.getCommand() + "\n", callback);
            }
            else
                ShowError.msg(this, "Unknown program!");
        }
	//
	// unknown command
	//
	else
        {
	    System.out.println("Unknown command: '" + cmd + "'.");
	}
    }

    //------------------------------------------------------------
    private void cmdShutdown()
    {
	if (gameChanged() && !askSaveGame())
	    return;

	System.out.println("Shutting down...");

	if (m_white_process != null)
        {
	    System.out.println("Stopping [" + m_white_name + " " +
			       m_white_version + "] process...");
	    m_white_process.destroy();
	}
	System.exit(0);
    }

    private void cmdNewProgram()
    {
        Program program = new Program();
        new EditProgramDialog(this, program, "Add New Program", true);

        if (program.m_name == null)  // user canceled
            return;

        // add the program to the list of programs
        m_programs.add(program);
        Program.save(m_programs);
    }

    private void cmdEditProgram()
    {
        if (m_programs.isEmpty())
        {
            ShowError.msg(this, "No programs, add a program first.");
            return;
        }

        ChooseProgramDialog dialog
            =  new ChooseProgramDialog(this, "Choose program to edit", m_programs);
        dialog.setVisible(true);
        Program program = dialog.getProgram();
        dialog.dispose();

        if (program == null)
            return;

        new EditProgramDialog(this, program, "Edit Program", false);

        Program.save(m_programs);
    }

    private void cmdDeleteProgram()
    {
        if (m_programs.isEmpty())
        {
            ShowError.msg(this, "No programs, add a program first.");
            return;
        }

        ChooseProgramDialog dialog
            =  new ChooseProgramDialog(this, "Choose program to delete",
                                       m_programs);
        dialog.setVisible(true);
        Program program = dialog.getProgram();
        dialog.dispose();

        if (program == null)
            return;

        if (!m_programs.remove(program))
            System.out.println("cmdDeleteProgram: program was not in list!");

        Program.save(m_programs);
    }

    private void cmdConnectLocalProgram()
    {
        ChooseProgramDialog dialog
            =  new ChooseProgramDialog(this, "Choose program to connect",
                                       m_programs);
        dialog.setVisible(true);
        Program program = dialog.getProgram();
        dialog.dispose();

	if (program == null) // user aborted
	    return;

        cmdConnectLocalProgram(program);
    }


    /** @note NOT CURRENTLY USED! */
    private void cmdConnectRemoteProgram()
    {
	int port = 20000;
	String hostname = "localhost";

        String remote = m_preferences.get("remote-host-name");
        String name = RemoteProgramDialog.show(this, remote);
        if (name == null) // user aborted
            return;

        hostname = name;
	System.out.print("Connecting to HTP program at [" + hostname +
			 "] on port " + port + "...");
	System.out.flush();

	try
        {
	    m_white_socket = new Socket(hostname, port);
	}
	catch (UnknownHostException e)
        {
	    ShowError.msg(this, "Unknown host: '" + e.getMessage() + "'");
            System.out.println("\nconnection attempt aborted.");
	    return;
	}
	catch (IOException e)
        {
	    ShowError.msg(this, "Error creating socket: '" 
                          + e.getMessage() + "'");
            System.out.println("\nconnection attempt aborted.");
	    return;
	}
	System.out.println("connected.");

	InputStream in;
	OutputStream out;
	try
        {
	    in = m_white_socket.getInputStream();
	    out = m_white_socket.getOutputStream();
	}
	catch (IOException e)
        {
	    ShowError.msg(this, "Error obtaining socket stream: " 
                          + e.getMessage());
	    m_white = null;
	    return;
	}
        m_preferences.put("remote-host-name", hostname);
	connectProgram(in, out);
    }

    //------------------------------------------------------------

    private void cmdConnectLocalProgram(Program program)
    {
	Runtime runtime = Runtime.getRuntime();

	String cmd = program.m_command;
	System.out.println("Executing '" + program.m_name + "':");
        System.out.println("Command = '" + cmd + "'");
        System.out.println("Working directory = '" + program.m_working + "'");

        File working = null;
        if (!program.m_working.trim().equals(""))
        {
            ShowError.msg(this,
                          "Working directory not implemented! " +
                          "Running with no working directory.");

//             working = new File(program.m_working);
//             if (!working.isDirectory())
//             {
//                 ShowError.msg(this, "Invalid working directory: '"
//                               + working.getName() + "'");
//             }
        }

	try
        {
            // Create command array with StringUtil::splitArguments
            // because Runtime.exec(String) uses a default StringTokenizer
            // which does not respect ".
            String[] cmdArray = StringUtils.splitArguments(cmd);
            // Make file name absolute, if working directory is not current
            // directory. With Java 1.5, it seems that Runtime.exec succeeds
            // if the relative path is valid from the current, but not from
            // the given working directory, but the process is not usable
            // (reading from its input stream immediately returns
            // end-of-stream)
            if (cmdArray.length > 0)
            {
                File file = new File(cmdArray[0]);
                // Only replace if executable is a path to a file, not
                // an executable in the exec-path
                if (file.exists())
                    cmdArray[0] = file.getAbsolutePath();
            }

            m_white_process = runtime.exec(cmdArray);
            //m_white_process = runtime.exec(cmdArray, null, workingDirectory);
            //m_white_process = runtime.exec(cmd);
	}
	catch (Throwable e)
        {
	    ShowError.msg(this, "Error starting " + program.m_name + ": '"
                          + e.getMessage() + "'");
	    return;
	}

        m_program = program;
        m_preferences.put("is-program-attached", true);
	m_preferences.put("attached-program", program.m_name);

 	Process proc = m_white_process;

	///////////////////////////////
	/// FIXME: DEBUGING!!! REMOVE!
	Thread blah = new Thread(new StreamCopy(false, proc.getErrorStream(),
						System.out, false));
	blah.start();
	///////////////////////////////

	connectProgram(proc.getInputStream(), proc.getOutputStream());
    }

    private void createAnalyzeDialog()
    {
        m_analyzeDialog = new AnalyzeDialog(this, this, m_analyzeCommands,
                                            m_messageDialogs);
        m_analyzeDialog.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    m_analyzeDialog.setVisible(false);
                    m_menubar.setAnalyzeVisible(false);
                }
            });
        m_analyzeDialog.setBoardSize(m_guiboard.getBoardSize().width);
        m_analyzeDialog.setSelectedColor(m_tomove);
        m_analyzeDialog.setVisible(true);
    }

    public void actionDisposeAnalyzeDialog()
    {
        if (m_analyzeDialog != null)
        {
            m_analyzeDialog.dispose();
            m_analyzeDialog = null;
            m_menubar.setAnalyzeVisible(false);
        }
    }

    private void connectProgram(InputStream in, OutputStream out)
    {
	m_shell = new HtpShell(this, this);
	m_shell.addWindowListener(new WindowAdapter()
	    {
		public void windowClosing(WindowEvent winEvt)
                {
		    m_menubar.setShellVisible(false);
		}
	    });
	m_white = new HtpController(in, out, m_shell, this);

        // get name and version information; block until
        // version is returned.  

        acquireSemaphore();
	htpName();
	htpVersion();  // releases semaphore when finished. 
        acquireSemaphore();
        releaseSemaphore();

	m_shell.setTitle("HexGui: [" + m_white_name + " "
                            + m_white_version + "] Shell");

        // get list of accepted commands; block until
        // this is completed.
        acquireSemaphore();
        htpAnalyzeCommands();   // releases semaphore when finished
        acquireSemaphore();
        releaseSemaphore();

        createAnalyzeDialog();

	m_toolbar.setProgramConnected(true);
	m_menubar.setProgramConnected(true);

	m_shell.setVisible(m_preferences.getBoolean("shell-show-on-connect"));
	m_analyzeDialog.setVisible(m_preferences.getBoolean("analyze-show-on-connect"));

	htpBoardsize(m_guiboard.getBoardSize());

        // play up to current move
	Node cur = m_root;
	while (cur != m_current)
        {
	    cur = cur.getChildContainingNode(m_current);
            if (cur.hasMove())
                htpPlay(cur.getMove());
            if (cur.hasSetup()) {
                playSetup(cur);
            }
	}
        htpShowboard();
    }

    private void cmdDisconnectProgram()
    {
	if (m_white == null)
	    return;

	htpQuit();
	try
        {
	    if (m_white_process != null)
            {
		m_white_process.waitFor();
		m_white_process = null;
	    }
	    if (m_white_socket != null)
            {
		m_white_socket.close();
		m_white_socket = null;
	    }
	    m_white = null;
	    m_shell.dispose();
	    m_shell = null;
            actionDisposeAnalyzeDialog();
            m_program = null;
	    m_menubar.setProgramConnected(false);
	    m_toolbar.setProgramConnected(false);
            m_preferences.put("is-program-attached", false);
	}
	catch (Throwable e)
        {
	    ShowError.msg(this, "Error: " + e.getMessage());
	}
    }

    private void cmdReconnectProgram()
    {
        Program prog = m_program;
        cmdDisconnectProgram();
        cmdConnectLocalProgram(prog);
    }

    //------------------------------------------------------------

    private void cmdNewGame()
    {
	if (gameChanged() && !askSaveGame())
	    return;

	String size = m_menubar.getSelectedBoardSize();
	Dimension dim = new Dimension(-1,-1);
	if (size.equals("Other..."))
        {
	    size = BoardSizeDialog.show(this, m_guiboard.getBoardSize());
	    if (size == null) return;
	}

	try
        {
	    StringTokenizer st = new StringTokenizer(size);
	    int w = Integer.parseInt(st.nextToken());
	    st.nextToken();
	    int h = Integer.parseInt(st.nextToken());
	    dim.setSize(w,h);
	}
	catch (Throwable t)
        {
	    ShowError.msg(this, "Size should be in format 'w x h'.");
	    return;
	}

	if (dim.width < 1 || dim.height < 1)
        {
	    ShowError.msg(this, "Invalid board size.");
	}
        else
        {
	    m_tomove = HexColor.BLACK;
            m_toolbar.setToMove(m_tomove.toString());

	    m_root = new Node();
	    m_current = m_root;
	    m_gameinfo = new GameInfo();
	    m_gameinfo.setBoardSize(dim);
            stopClock(HexColor.BLACK);
            stopClock(HexColor.WHITE);
            m_blackClock.setElapsed(0);
            m_whiteClock.setElapsed(0);
            setComment(m_current);

	    m_file = null;
	    setGameChanged(false);
	    setFrameTitle();

	    m_guiboard.initSize(dim.width, dim.height);
	    m_guiboard.repaint();

	    m_preferences.put("gui-board-width", dim.width);
	    m_preferences.put("gui-board-height", dim.height);

	    m_toolbar.updateButtonStates(m_current);
            m_menubar.updateMenuStates(m_current);

            htpBoardsize(m_guiboard.getBoardSize());
            htpShowboard();
	}
    }

    private boolean cmdSaveGame()
    {
	if (m_file == null)
	    m_file = showSaveAsDialog();

	if (m_file != null)
        {
	    System.out.println("Saving to file: " + m_file.getName());
	    if (save(m_file))
            {
		setGameChanged(false);
		setFrameTitle();
		m_preferences.put("path-save-game", m_file.getPath());
		return true;
	    }
	}
	return false;
    }

    private boolean cmdSaveGameAs()
    {
	File file = showSaveAsDialog();
	if (file == null)
	    return false;

	m_file = file;
	return cmdSaveGame();
    }

    private void cmdSavePositionAs()
    {
        File file = showSaveAsDialog();
        if (file != null)
            savePosition(file);
    }

    private void cmdLoadGame()
    {
	if (gameChanged() && !askSaveGame())
	    return;
	File file = showOpenDialog();
	if (file != null)
            loadGame(file);
    }

    private void cmdPrintPreview()
    {
        JFrame frame = new JFrame();
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container con = frame.getContentPane();

        PrintPreview pp = new PrintPreview(m_guiboard);
        con.add(pp, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
        frame.toFront();
    }

    private void cmdPrint()
    {
        Print.run(this, m_guiboard);
    }

    private void cmdAbout()
    {
        m_about.setVisible(true);
    }

    //------------------------------------------------------------

    private void cmdGuiToolbarVisible()
    {
	boolean visible = m_menubar.getToolbarVisible();
	m_toolbar.setVisible(visible);
    }

    private void cmdGuiShellVisible()
    {
	if (m_shell == null) return;
	boolean visible = m_menubar.getShellVisible();
	m_shell.setVisible(visible);
    }

    private void cmdGuiAnalyzeVisible()
    {
	if (m_analyzeDialog == null) return;
	boolean visible = m_menubar.getAnalyzeVisible();
	m_analyzeDialog.setVisible(visible);
    }

    private void cmdGuiBoardDrawType()
    {
	String type = m_menubar.getCurrentBoardDrawType();
	System.out.println(type);
	m_guiboard.setDrawType(type);
	m_guiboard.repaint();
    }

    private void cmdGuiBoardOrientation()
    {
	String type = m_menubar.getCurrentBoardOrientation();
	System.out.println(type);
	m_guiboard.setOrientation(type);
	m_guiboard.repaint();
    }

    private void cmdClearMarks()
    {
        m_guiboard.clearMarks();
        m_guiboard.repaint();
    }

    private void cmdShowPreferences()
    {
        new PreferencesDialog(this, m_preferences);
    }

    private void cmdToggleToMove()
    {
        m_tomove = m_tomove.otherColor();
        m_toolbar.setToMove(m_tomove.toString());
        m_menubar.setToMove(m_tomove.toString());
    }

    private void cmdSetToMove()
    {
        m_tomove = HexColor.get(m_menubar.getToMove());
        m_toolbar.setToMove(m_tomove.toString());
    }

    //------------------------------------------------------------

    public void actionClearAnalyzeCommand()
    {
        
    }

    public void actionSetAnalyzeCommand(AnalyzeCommand command)
    {
        actionSetAnalyzeCommand(command, false, true, true, false);
    }

    public void actionSetAnalyzeCommand(AnalyzeCommand command,
                                        boolean autoRun, boolean clearBoard,
                                        boolean oneRunOnly,
                                        boolean reuseTextWindow)
    {
        AnalyzeType type = command.getType();
        if (command.needsPointArg())
        {
            Vector<HexPoint> selected = getSelectedCells();
            if (selected.size() < 1)
            {
                m_statusbar.setMessage("Please select a cell before " +
                                       "running.");
                return;
            }
            command.setPointArg(selected.get(0));
        }
        if (command.needsPointListArg())
        {
            Vector<HexPoint> selected = getSelectedCells();
            if (type == AnalyzeType.VC && selected.size() != 2)
            {
                m_statusbar.setMessage("Please select a pair of cells before " +
                                       "running.");
                return;
            }
            PointList blah = new PointList(selected);
            command.setPointListArg(blah);
        }
        String cmd = command.replaceWildCards(m_tomove);
        String cleaned = StringUtils.cleanWhiteSpace(cmd.trim());
        String args[] = cleaned.split(" ");
	String c = args[0];
        m_curAnalyzeCommand = command;

        Runnable cb = null;
        switch(type)
        {
        case GROUP:
            cb = new Runnable() { public void run() { cbGroupGet(); } };
            break;
        case GFX:
            cb = new Runnable() { public void run() { cbGfx(); } };
            break;
        case INFERIOR:
             cb = new Runnable() { public void run() {cbShowInferiorCells();}};
             break;
        case MOVE:
            cb = new Runnable() { public void run() { cbGenMove(); } };
            break;
        case PLIST:
            cb = new Runnable() { public void run() { cbDisplayPointList(); } };
            break;
        case PSPAIRS:
            cb = new Runnable() { public void run() { cbDisplayPointText(); } };
            break;
        case PARAM:
            cb = new Runnable() { public void run() { cbEditParameters(); } };
            break;
        case VC:
            cb = new Runnable() { public void run() { cbVCs(); } };
            break;
        case STRING:
            cb = new Runnable() { public void run() { cbString(); } };
            break;
        case VAR:
            cb = new Runnable() { public void run() { cbVar(); } };
            break;
        }            
        // if (c.equals("dfpn-get-bounds"))
        //     cb = new Runnable() { public void run() { cbDfpnDisplayBounds();} };
        // else if (c.equals("book-scores"))
        //     cb = new Runnable() { public void run() { cbDisplayBookScores(); } };
        // else if (c.equals("eval-resist"))
        //     cb = new Runnable() { public void run() { cbEvalResist(); } };
        Runnable callback = null;
        if (cb != null)
            callback = new GuiRunnable(cb);
        sendCommand(cmd + "\n", callback);
    }

    /** HtpShell Callback.
        By the name of the command it choose the proper callback function.
        Arguments are passed as given.
    */
    public void commandEntered(String cmd)
    {
        sendCommand(cmd, null);
    }

    //----------------------------------------------------------------------

    private boolean commandNeedsToLockGUI(String cmd)
    {
        if ((cmd.length() > 7 && cmd.substring(0, 7).equals("genmove")) ||
            (cmd.length() > 15 && cmd.substring(0, 15).equals("dfs-solve-state")) ||
            (cmd.length() > 16 && cmd.substring(0, 16).equals("dfpn-solve-state")) ||
            (cmd.length() > 23 && cmd.substring(0, 23).equals("dfs-solver-find-winning")) ||
            (cmd.length() > 24 && cmd.substring(0, 24).equals("dfpn-solver-find-winning")))
            return true;
        return false;
    }

    private void lockGUI()
    {
        m_locked = true;
        m_toolbar.lockToolbar();
    }

    private void unlockGUI()
    {
        m_toolbar.unlockToolbar(m_current);
        m_locked = false;
    }

    /** A (command, callback) pair. */
    private class HtpCommand
    {
        public HtpCommand()
        {
        }

        public HtpCommand(String cmd, Runnable callback)
        {
            this.str = cmd;
            this.callback = callback;
        }

        public String str;
        public Runnable callback;
    }

    /** Waits for commands to be added to the queue, then processes
        each in turn. */
    private class CommandHandler
        implements Runnable
    {

        public CommandHandler(Component parent, 
                              ArrayBlockingQueue<HtpCommand> queue)
        {
            m_parent = parent;
            m_queue = queue;
        }

        public void run()
        {
            while (true) 
            {
                HtpCommand cmd = null;
                try 
                {
                    // block until queue contains an element
                    cmd = m_queue.take();
                }
                catch(InterruptedException e)
                {
                    System.out.println("INTERUPTED! HUH?");
                }

                if (m_white != null && m_white.connected()) 
                {
                    if (commandNeedsToLockGUI(cmd.str))
                        lockGUI();
                    
                    try  {
                        m_white.sendCommand(cmd.str);
                        if (cmd.callback != null) {
                            cmd.callback.run();
                        }
                    }
                    catch (HtpError e) {
                        System.out.println("Caught error '" 
                                           + e.getMessage() + "'");
                        ShowError.msg(m_parent, e.getMessage());
                    }
                    
                    if (commandNeedsToLockGUI(cmd.str))
                        unlockGUI();
                }
                else
                {
                    System.out.println("Not sending to disconnected: '" 
                                       + cmd.str.trim() + "'");
                }
            }
        }

        Component m_parent;
        ArrayBlockingQueue<HtpCommand> m_queue;
    }

    private void sendCommand(String cmd, Runnable callback)
    {
	if (m_white == null)
	    return;

        try {
            System.out.println("sendCommand: '" + cmd.trim() + "'");
            m_htp_queue.put(new HtpCommand(cmd, callback));
        }
        catch (InterruptedException e)
        {
            System.out.println("Interrupted while adding!");
        }
    }

    // FIXME: add callback?
    private void htpQuit()
    {
	sendCommand("quit\n", null);
    }

    private void htpName()
    {
	Runnable cb = new Runnable() { public void run() { cbName(); } };
	sendCommand("name\n", cb);
    }

    private void htpVersion()
    {
	Runnable cb = new Runnable() { public void run() { cbVersion(); } };
	sendCommand("version\n", cb);
    }

    private void htpAnalyzeCommands()
    {
	Runnable cb = new Runnable() 
            { public void run() { cbAnalyzeCommands(); } };
	sendCommand("hexgui-analyze_commands\n", cb);
    }

    private void htpShowboard()
    {
        sendCommand("showboard\n", null);
    }

    private void htpPlay(Move move)
    {
	sendCommand("play " + move.getColor().toString() +
		    " " + move.getPoint().toString() + "\n", null);
    }

    private void htpUndo()
    {
	sendCommand("undo\n", null);
    }

    private void htpGenMove(HexColor color)
    {
        if (! checkBoardSizeSupported())
            return;
        m_statusbar.setMessage(format("{0} is thinking...", m_white_name));
	Runnable callback = new GuiRunnable(new Runnable()
	    {
		public void run() { cbGenMove(); }
	    });
 	sendCommand("genmove " + color.toString() + "\n", callback);
    }

    private void htpBoardsize(Dimension size)
    {
	Runnable callback = new Runnable()
	    {
		public void run() {
                    m_unsupportedBoardSize = ! m_white.wasSuccess();
                    checkBoardSizeSupported();
                }
	    };
        sendCommand("boardsize " + size.width + " " + size.height + "\n",
                    callback);
        m_statusbar.setMessage("New game");
    }
    
    //
    // Callbacks
    //
    public void cbName()
    {
	String str = m_white.getResponse();
	// FIXME: handle errors!
	m_white_name = str.trim();
    }

    public void cbVersion()
    {
	String str = m_white.getResponse();
	// FIXME: handle errors!
	m_white_version = str.trim();
        releaseSemaphore();
    }

    private void cbAnalyzeCommands()
    {
        String programAnalyzeCommands = m_white.getResponse();
        try
        {
            m_analyzeCommands 
                = AnalyzeDefinition.read(programAnalyzeCommands);
        }
        catch (ErrorMessage e)
        {
            ShowError.msg(this, "Could not parse analyze commands!");
        }
        releaseSemaphore();
    }

    public void cbGenMove()
    {
        if (!m_white.wasSuccess())
            return;
        m_guiboard.clearMarks();
	String str = m_white.getResponse();
	HexPoint point = HexPoint.get(str.trim());
	if (point == null)
        {
	    System.out.println("Invalid move!!");
	}
        else
        {
	    play(new Move(point, m_tomove));
	}
    }

    public void cbDisplayPointList()
    {
	if (!m_white.wasSuccess())
	    return;
	String str = m_white.getResponse();
	Vector<HexPoint> points = StringUtils.parsePointList(str);
        m_guiboard.clearMarks();
        for (int i=0; i<points.size(); i++)
        {
	    m_guiboard.setAlphaColor(points.get(i), Color.green);
	}
	m_guiboard.repaint();
    }

    private void cbDfpnDisplayBounds()
    {
	if (!m_white.wasSuccess()) 
	    return;
	String str = m_white.getResponse();
        showDfpnBounds(str);
	m_guiboard.repaint();
    }

    public void cbGroupGet()
    {
        if (!m_white.wasSuccess())
	    return;
	String str = m_white.getResponse();
	Vector<HexPoint> points = StringUtils.parsePointList(str);
        m_guiboard.clearMarks();
        if (points.size() > 0)
        {
            m_guiboard.setAlphaColor(points.get(0), Color.blue);
            for (int i=1; i<points.size(); i++)
            {
                m_guiboard.setAlphaColor(points.get(i), Color.green);
            }
        }
	m_guiboard.repaint();
    }

    public void cbGfx()
    {
	if (!m_white.wasSuccess())
	    return;
        m_guiboard.clearMarks();
        m_guiboard.aboutToDirtyStones();
        
        String fx = m_white.getResponse();
        int inf = fx.indexOf("INFLUENCE");
        if (inf < 0)
            return;
        boolean hasText = false;
        int text = fx.indexOf("TEXT");
        if (text < 0)
            text = fx.length();
        else {
            hasText = true;
        }
        
        Vector<Pair<String, String> > pairs =
            StringUtils.parseStringPairList(fx.substring(inf + 10, text));
        for (int i=0; i<pairs.size(); i++)
        {
	    HexPoint point = HexPoint.get(pairs.get(i).first);
            String value = pairs.get(i).second;
            float v = new Float(value).floatValue();
            m_guiboard.setAlphaColor(point, new Color(0, v, 1-v), 0.7f);
	}
        if (hasText)
            m_statusbar.setMessage(fx.substring(text+5));
	m_guiboard.repaint();
    }

    public void cbShowInferiorCells()
    {
	if (!m_white.wasSuccess()) 
	    return;
        m_guiboard.clearMarks();
        m_guiboard.aboutToDirtyStones();
        showInferiorCells(m_white.getResponse());
	m_guiboard.repaint();
    }

    public void cbVCs()
    {
	if (!m_white.wasSuccess()) 
            return;
        String str = m_white.getResponse();
        Vector<VC> vcs = StringUtils.parseVCList(str);
        new VCDisplayDialog(this, m_guiboard, vcs);
    }

    public void cbString()
    {
	if (!m_white.wasSuccess()) 
            return;
        String showText = m_white.getResponse();
        String title = m_curAnalyzeCommand.getResultTitle();
        if (showText != null)
        {
            if (showText.indexOf("\n") < 0)
            {
                if (showText.trim().equals(""))
                    showText = "(empty response)";
                m_statusbar.setMessage(format("{0}: {1}", title, showText));
            }
            else
            {
                HexPoint pointArg = null;
                m_showAnalyzeText.show(m_curAnalyzeCommand.getType(), 
                                       pointArg, title, showText, false);
            }
        }
    }

    public void cbVar()
    {
        if (!m_white.wasSuccess())
            return;
        String str = m_white.getResponse();
        Vector<HexPoint> points = StringUtils.parsePointList(str, " ");
        m_guiboard.clearMarks();
        m_guiboard.aboutToDirtyStones();
        HexColor color = m_tomove;
        for (int i = 0; i < points.size(); i++)
        {
            m_guiboard.setColor(points.get(i), color);
            m_guiboard.setText(points.get(i), Integer.toString(i + 1));
            color = color.otherColor();
        }
	m_guiboard.repaint();
    }

    public void cbDisplayPointText()
    {
	if (!m_white.wasSuccess()) 
            return;
	String str = m_white.getResponse();
        Vector<Pair<String, String> > pairs =
            StringUtils.parseStringPairList(str);
        m_guiboard.clearMarks();
        for (int i=0; i<pairs.size(); i++)
        {
	    HexPoint point = HexPoint.get(pairs.get(i).first);
            String value = pairs.get(i).second;
            m_guiboard.setText(point, value);
	}
	m_guiboard.repaint();
    }

    public void cbDisplayBookScores()
    {
	if (!m_white.wasSuccess()) 
            return;
	String str = m_white.getResponse();
        Vector<Pair<String, String> > pairs =
            StringUtils.parseStringPairList(str);
        m_guiboard.clearMarks();
        for (int i=0; i<pairs.size(); i++)
        {
	    HexPoint point = HexPoint.get(pairs.get(i).first);
            String value = pairs.get(i).second;
            m_guiboard.setText(point, value);
            if (i == 0)
                m_guiboard.setAlphaColor(point, Color.red);
            else if (1 <= i && i <= 3)
                m_guiboard.setAlphaColor(point, Color.green);
	}
	m_guiboard.repaint();
    }

    public void cbEvalResist()
    {
	if (!m_white.wasSuccess()) 
            return;
	String str = m_white.getResponse();
        Vector<Pair<String, String> > pairs =
            StringUtils.parseStringPairList(str);
        String res = "";
        String rew = "";
        String reb = "";
        m_guiboard.clearMarks();
        for (int i=0; i<pairs.size(); i++)
        {
            if (pairs.get(i).first.equals("res"))
            {
                res = pairs.get(i).second;
            }
            else if (pairs.get(i).first.equals("rew"))
            {
                rew = pairs.get(i).second;
            }
            else if (pairs.get(i).first.equals("reb"))
            {
                reb = pairs.get(i).second;
            }
            else
            {
                HexPoint point = HexPoint.get(pairs.get(i).first);

                String value = pairs.get(i).second;
                m_guiboard.setText(point, value);
            }
	}
	m_guiboard.repaint();
        m_statusbar.setMessage("Resistance: " + res +
                               " (" + rew + " - " + reb + ")");
    }

    public void cbEditParameters()
    {
        if (!m_white.wasSuccess()) 
            return;
	String response = m_white.getResponse();
        ParameterDialog.editParameters(m_curAnalyzeCommand.getCommand(), this,
                                       "Edit Parameters", response, m_white,
                                       m_messageDialogs);
    }

    //==================================================
    // gfx commands
    //==================================================
    public void guifx(String fx)
    {
        System.out.println("gogui-gfx:\n'" + fx + "'");
        
        if (fx.length() > 3 && fx.substring(0, 3).equals("uct"))
            guifx_uct(fx.substring(3));
        else if (fx.length() > 2 && fx.substring(0, 2).equals("ab"))
            guifx_ab(fx.substring(2));
        else if (fx.length() > 4 && fx.substring(0, 4).equals("dfpn"))
            guifx_dfpn(fx.substring(4));
        else if (fx.length() > 6 && fx.substring(0, 6).equals("solver"))
            guifx_solver(fx.substring(6));
    }

    private void guifx_uct(String fx)
    {
        String[] tk = fx.trim().split(" ");
        int i=0;

        m_guiboard.clearMarks();
        m_guiboard.aboutToDirtyStones();

        /** @todo Fix this to parse like guifx_ab() and
            guifx_solver(). */

        //////////////////////////////////////
        // display variation
        for (; i < tk.length; ++i) 
        {
            String s = tk[i].trim();
            if (s.equals("VAR"))
                break;
        }
        if (i == tk.length) 
            return;
        ++i; // skip "VAR";

        Vector<HexPoint> var = new Vector<HexPoint>();
        Vector<HexColor> col = new Vector<HexColor>();
        for (; i < tk.length; ) 
        {
            String s = tk[i].trim();
            if (s.equals("INFLUENCE"))
                break;
            ++i; // skip 'B' and 'W'

            col.add((s.charAt(0) == 'B') ? HexColor.BLACK : HexColor.WHITE);
            HexPoint point = HexPoint.get(tk[i++].trim());
            var.add(point);
        }
        
        m_guiboard.setColor(var.get(0), col.get(0));
        m_guiboard.setAlphaColor(var.get(0), Color.cyan);
        if (var.size() > 1)
        {
            m_guiboard.setColor(var.get(1), col.get(1));
            m_guiboard.setAlphaColor(var.get(1), Color.blue);
        }

        /////////////////////////////////////////
        // display score/search counts
        
        TreeMap<HexPoint, String> map = new TreeMap<HexPoint, String>();

        ++i; // skip 'INFLUENCE'
        for (; i<tk.length; ) {
            String s = tk[i].trim();
            if (s.equals("LABEL"))
                break;

            HexPoint point = HexPoint.get(tk[i++].trim());
            String score = tk[i++].trim();
            map.put(point, score);
            if (score.equals("W"))
                m_guiboard.setAlphaColor(point, Color.green);
            else if (score.equals("L"))
                m_guiboard.setAlphaColor(point, Color.red);
        }

        ++i; // skip "LABEL";
        for (; i<tk.length; ) {
            String s = tk[i].trim();
            if (s.equals("TEXT"))
                break;

            HexPoint point = HexPoint.get(tk[i++].trim());
            
            String old = map.get(point);
            if (old == null) old = "";
            map.put(point, old+"@"+tk[i++].trim());
        }
        
	Iterator<Map.Entry<HexPoint,String> > it = map.entrySet().iterator();
	while(it.hasNext()) {
	    Map.Entry<HexPoint,String> e = it.next();
            m_guiboard.setText(e.getKey(), e.getValue());
	}

        m_guiboard.repaint();
        m_statusbar.setMessage(fx.substring(fx.indexOf("TEXT")+5));
    }

    private void guifx_ab(String fx)
    {
        m_guiboard.clearMarks();
        m_guiboard.aboutToDirtyStones();

        int var = fx.indexOf("VAR");
        int label = fx.indexOf("LABEL");
        int text = fx.indexOf("TEXT");

        Vector<Pair<HexColor, HexPoint> > vr 
            =  StringUtils.parseVariation(fx.substring(var+3, label));
        if (vr.size() > 0)
        {
            m_guiboard.setColor(vr.get(0).second, vr.get(0).first);
            m_guiboard.setAlphaColor(vr.get(0).second, Color.green);
            if (vr.size() >= 2) 
            {
                m_guiboard.setColor(vr.get(1).second, vr.get(1).first);
                m_guiboard.setAlphaColor(vr.get(1).second, Color.red);
            }
        }
        String label_str = fx.substring(label+5, text).trim();
        Vector<Pair<String, String> > labels =
            StringUtils.parseStringPairList(label_str);
        for (int i = 0; i < labels.size(); ++i) 
        {
            HexPoint pt = HexPoint.get(labels.get(i).first);
            m_guiboard.setText(pt, labels.get(i).second);
        }
        m_guiboard.repaint();
        m_statusbar.setMessage(fx.substring(text+5));
    }

    private void guifx_solver(String fx)
    {
        m_guiboard.clearMarks();
        m_guiboard.aboutToDirtyStones();
        m_statusbar.setProgressVisible(true);

        int var = fx.indexOf("VAR");
        int label = fx.indexOf("LABEL");
        int text = fx.indexOf("TEXT");

        Vector<Pair<HexColor, HexPoint> > vr 
            =  StringUtils.parseVariation(fx.substring(var+3, label));
        for (int i = 0; i < vr.size(); ++i) 
        {
            m_guiboard.setColor(vr.get(i).second, vr.get(i).first);
            m_guiboard.setText(vr.get(i).second, Integer.toString(i+1));
        }

        String label_str = fx.substring(label+5, text).trim();
        showInferiorCells(label_str);
    
        String prog_str = fx.substring(text+4).trim();
        String[] levels = prog_str.split(" ");

        double contribution = 1.0;
        double progress = 0.0;
        for (int i = 0; i < levels.length; ++i)
        {
            String[] nums = levels[i].trim().split("/");
            int cur = Integer.decode(nums[0]).intValue();
            int max = Integer.decode(nums[1]).intValue();
            progress += contribution*cur/max;
            contribution *= 1.0/max;
        }
        m_guiboard.repaint();
        m_statusbar.setMessage(fx.substring(text+5));
        m_statusbar.setProgress(progress);
    }

    private void guifx_dfpn(String fx)
    {
        m_guiboard.clearMarks();
        m_guiboard.aboutToDirtyStones();

        int var = fx.indexOf("VAR");
        int label = fx.indexOf("LABEL");
        int text = fx.indexOf("TEXT");

        Vector<Pair<HexColor, HexPoint> > vr 
            =  StringUtils.parseVariation(fx.substring(var+3, label));
        for (int i = 0; i < vr.size(); ++i) 
        {
            m_guiboard.setColor(vr.get(i).second, vr.get(i).first);
            m_guiboard.setText(vr.get(i).second, Integer.toString(i+1));
            m_guiboard.setAlphaColor(vr.get(i).second, Color.blue);
        }
        String label_str = fx.substring(label+5, text).trim();
        showDfpnBounds(label_str);

        m_guiboard.repaint();
        m_statusbar.setMessage(fx.substring(text+5));
    }

    private void showDfpnBounds(String str)
    {
        Vector<Pair<String, String> > pairs =
            StringUtils.parseStringPairList(str);
        for (int i = 0; i < pairs.size(); i++)
        {
	    HexPoint point = HexPoint.get(pairs.get(i).first);
            String value = pairs.get(i).second;
            m_guiboard.setText(point, value);
            if (value.trim().equals("W"))
                m_guiboard.setAlphaColor(point, Color.green);
            else if (value.trim().equals("L"))
                m_guiboard.setAlphaColor(point, Color.red);
        }    
    }

    /** Draws the inferior cells to the gui board. */
    private void showInferiorCells(String str)
    {
        Vector<Pair<String, String> > pairs =
            StringUtils.parseStringPairList(str);
        for (int i = 0; i < pairs.size(); i++)
        {
	    HexPoint point = HexPoint.get(pairs.get(i).first);
            String value = pairs.get(i).second;

            if (value.charAt(0) == 'f')        // fill-in
            {
                assert(3 == value.length());
                if (value.charAt(1) == 'd')          // dead
                    m_guiboard.setAlphaColor(point, Color.cyan);
                else if (value.charAt(1) == 'p')     // permanently inferior
                    m_guiboard.setAlphaColor(point, Color.gray);
                else                                 // captured
                {
                    assert(value.charAt(1) == 'c');
                    m_guiboard.setAlphaColor(point, Color.red);
                }
                if (value.charAt(2) == 'b')
                    m_guiboard.setColor(point, HexColor.BLACK);
                else
                {
                    assert(value.charAt(2) == 'w');
                    m_guiboard.setColor(point, HexColor.WHITE);
                }
            }
            else if (value.charAt(0) == 'i')   // ignorable
            {
                assert(4 <= value.length());
                if (value.charAt(1) == 'v')          // vulnerable
                    m_guiboard.setAlphaColor(point, Color.green);
                else if (value.charAt(1) == 'r')     // reversible
                    m_guiboard.setAlphaColor(point, Color.magenta);
                else                                 // dominated
                {
                    assert(value.charAt(1) == 'd');
                    m_guiboard.setAlphaColor(point, Color.yellow);
                }
                assert(value.charAt(2) == '[' &&
                       value.charAt(value.length()-1) == ']');
                String pts = value.substring(3, value.length()-1);
                Vector<HexPoint> pp = StringUtils.parsePointList(pts,"-");
                for (int j=0; j<pp.size(); ++j)
                    m_guiboard.addArrow(point, pp.get(j));
            }
            else                               // not in consider set
            {
                assert(value.charAt(0) == 'x');
                m_guiboard.setAlphaColor(point, Color.gray);
            }
	}
    }

    //------------------------------------------------------------

    /** Callback from GuiBoard.
	Handle a mouse click.
    */
    public void fieldClicked(HexPoint point, boolean ctrl, boolean shift)
    {
        // do not modify the board in any way if an htp command is in progress!
        if (m_locked) {
            m_statusbar.setMessage("Board is locked until HTP command is completed.");
            return;
        }

        if (m_guiboard.areStonesDirty())
        {
            m_guiboard.clearMarks();
        }
        if (ctrl)
        {
            if (!shift)
            {
                for (int i=0; i<m_selected_cells.size(); i++)
                {
                    HexPoint p = m_selected_cells.get(i);
                    m_guiboard.setSelected(p, false);
                }
                m_selected_cells.clear();
                m_guiboard.setSelected(point, true);
                m_selected_cells.add(point);
            }
            else
            {
                int found_at = -1;
                for (int i=0; i<m_selected_cells.size() && found_at == -1; i++)
                {
                    if (m_selected_cells.get(i) == point)
                        found_at = i;
                }

                if (found_at != -1)
                {
                    m_guiboard.setSelected(point, false);
                    m_selected_cells.remove(found_at);
                }
                else
                {
                    m_guiboard.setSelected(point, true);
                    m_selected_cells.add(point);
                }
            }

            m_guiboard.repaint();

        }
        else
        {
            String context = m_toolbar.getClickContext();
            if (context.equals("play"))
            {
                if (m_guiboard.getColor(point) == HexColor.EMPTY)
                {
                    humanMove(new Move(point, m_tomove));
                }
            }
            else if (context.equals("black"))
            {
                if (m_guiboard.getColor(point) == HexColor.EMPTY)
                {
                    addSetupMove(new Move(point, HexColor.BLACK));
                }
            }
            else if (context.equals("white"))
            {
                if (m_guiboard.getColor(point) == HexColor.EMPTY)
                {
                    addSetupMove(new Move(point, HexColor.WHITE));
                }
            }
        }
    }

    public void fieldDoubleClicked(HexPoint point, boolean ctrl, boolean shift)
    {
    }

    public Vector<HexPoint> getSelectedCells()
    {
        return m_selected_cells;
    }

    public HexColor getColorToMove()
    {
        return m_tomove;
    }

    public void humanMove(Move move)
    {
	play(move);
	htpPlay(move);
        htpShowboard();
        if (! m_guiboard.isBoardFull()
            && m_preferences.getBoolean("auto-respond")
            && m_program != null)
            htpGenMove(m_tomove);
    }

    private void play(Move move)
    {
        if (move.getPoint() == HexPoint.RESIGN) return;

        // see if variation already exists; if so, do not add a duplicate
        int variation = -1;
        for (int i=0; i<m_current.numChildren(); i++)
        {
            Node child = m_current.getChild(i);
            if (child.hasMove() && move.equals(child.getMove()))
            {
                variation = i;
                break;
            }
	}

	if (variation != -1)
        {
            // variation already exists
	    m_current = m_current.getChild(variation);

	}
        else
        {
            if (move.getPoint() == HexPoint.SWAP_PIECES)
            {
                if (!m_current.isSwapAllowed())
                {
                    ShowError.msg(this, "Swap move not allowed!");
                    return;
                }
            }
            else
            {
                if (m_guiboard.getColor(move.getPoint()) !=  HexColor.EMPTY)
                {
                    ShowError.msg(this, "Cell '" + move.getPoint().toString() +
                                  "' already occupied.");
                    return;
                }
            }

            // add new node
	    Node node = new Node(move);
	    m_current.addChild(node);
	    m_current = node;
	}

        stopClock(m_tomove);

        if (m_guiboard.isYBoard() 
            || m_current.getMove().getPoint() != HexPoint.SWAP_PIECES)
            cmdToggleToMove();
        startClock(m_tomove);

        if (m_guiboard.isYBoard() && move.getPoint() == HexPoint.SWAP_PIECES)
            m_guiboard.swapColors();
        else
            m_guiboard.setColor(m_current.getMove().getPoint(),
                                m_current.getMove().getColor());

        m_guiboard.clearMarks();
	markLastPlayedStone();
	m_toolbar.updateButtonStates(m_current);
        m_menubar.updateMenuStates(m_current);
        m_statusbar.setMessage(m_current.getDepth() + " " 
                               + move.getColor().toString() + " " 
                               + move.getPoint().toString());
        setComment(m_current);

	setGameChanged(true);
	setFrameTitle();

	m_guiboard.paintImmediately();
        if (m_current.hasLabel())
        {
            displayLabels(m_current);
            m_guiboard.paintImmediately();
        }
    }

    private void addSetupMove(Move move)
    {
        // if current node doesn't have setup info, create a setup node
        if (!m_current.hasSetup())
        {
            Node setup = new Node();
            m_current.addChild(setup);
            m_current = setup;
        }

        m_guiboard.clearMarks();

        // add the setup stone to the set of setup stones
        m_current.addSetup(move.getColor(), move.getPoint());
        m_current.setPlayerToMove(move.getColor());
        
        // and set the color to play next
        m_tomove = move.getColor();
        m_toolbar.setToMove(m_tomove.toString());

        m_guiboard.setColor(move.getPoint(), move.getColor());
        m_guiboard.paintImmediately();

        htpPlay(move);

        m_statusbar.setMessage("Added setup stone (" + move.getColor().toString() +
                               ", " + move.getPoint().toString() + ")");

        setGameChanged(true);
        setFrameTitle();
    }

    //----------------------------------------------------------------------

    private void displayLabels(Node node)
    {
        Vector<String> labels = node.getLabels();
        for (int i = 0; i < labels.size(); ++i)
        {
            String lb = labels.get(i);
            String[] strs = lb.split(":");
            HexPoint p = HexPoint.get(strs[0].trim());
            m_guiboard.setText(p, strs[1].trim());
        }
    }

    private void playSetup(Node node)
    {
        Vector<HexPoint> black = node.getSetup(HexColor.BLACK);
        Vector<HexPoint> white = node.getSetup(HexColor.WHITE);
        for (int j=0; j<black.size(); j++)
        {
            HexPoint point = black.get(j);
            m_guiboard.setColor(point, HexColor.BLACK);
            htpPlay(new Move(point, HexColor.BLACK));
        }
        for (int j=0; j<white.size(); j++)
        {
            HexPoint point = white.get(j);
            m_guiboard.setColor(point, HexColor.WHITE);
            htpPlay(new Move(point, HexColor.WHITE));
        }
    }

    private void undoSetup(Node node)
    {
        Vector<HexPoint> black = node.getSetup(HexColor.BLACK);
        Vector<HexPoint> white = node.getSetup(HexColor.WHITE);
        for (int j=0; j<black.size(); j++)
        {
            HexPoint point = black.get(j);
            m_guiboard.setColor(point, HexColor.EMPTY);
            htpUndo();
        }
        for (int j=0; j<white.size(); j++)
        {
            HexPoint point = white.get(j);
            m_guiboard.setColor(point, HexColor.EMPTY);
            htpUndo();
        }
    }

    private void playNode(Node node)
    {
        if (node.hasMove())
        {
            Move move = node.getMove();
            m_guiboard.setColor(move.getPoint(), move.getColor());
            htpPlay(move);
            if (move.getPoint() == HexPoint.RESIGN)
            {
                m_statusbar.setMessage(node.getDepth() + " " 
                                       + move.getColor().toString() 
                                       + " resigned.");
            }
            else
            {
                m_statusbar.setMessage(node.getDepth() + " "
                                       + move.getColor().toString() + " "
                                       + move.getPoint().toString());
            }
        }
        if (node.hasSetup())
        {
            playSetup(node);
        }
    }

    private void undoNode(Node node)
    {
        if (node.hasMove())
        {
            Move move = node.getMove();
            if (m_guiboard.isYBoard() 
                && move.getPoint() == HexPoint.SWAP_PIECES)
                m_guiboard.swapColors();
            else
                m_guiboard.setColor(move.getPoint(), HexColor.EMPTY);
            htpUndo();
        }
        if (node.hasSetup())
        {
            undoSetup(node);
            m_statusbar.setMessage("Undo setup stones");
        }
    }

    private void refreshGuiForBoardState()
    {
	markLastPlayedStone();
	m_guiboard.repaint();
	m_toolbar.updateButtonStates(m_current);
        m_menubar.updateMenuStates(m_current);
        setComment(m_current);
        if (m_current.hasMove())
        {
            Move move = m_current.getMove();
            m_statusbar.setMessage(m_current.getDepth() + " " 
                                   + move.getColor().toString() + " " 
                                   + move.getPoint().toString());
        }
        if (m_current.hasLabel())
            displayLabels(m_current);
        if (m_current.hasCount())
            System.out.println("Count: " + m_current.getCount());
        determineColorToMove();
        htpShowboard();
    }

    private void forward(int n)
    {
        m_guiboard.clearMarks();

	for (int i=0; i<n; ++i)
        {
	    Node child = m_current.getChild();
	    if (child == null) break;

            playNode(child);
            m_current = child;
	}
        stopClock();
        refreshGuiForBoardState();
    }

    private void backward(int n)
    {
        m_guiboard.clearMarks();

	for (int i=0; i<n; ++i)
        {
	    if (m_current == m_root) break;

            undoNode(m_current);
	    m_current = m_current.getParent();
            if (m_current.hasSetup())
                playSetup(m_current);
	}
        stopClock();
        refreshGuiForBoardState();
    }

    private void up()
    {
	if (m_current.getNext() != null)
        {
            m_guiboard.clearMarks();
            undoNode(m_current);
            m_current = m_current.getNext();
            playNode(m_current);

            stopClock();
            refreshGuiForBoardState();
	}
    }

    private void down()
    {
	if (m_current.getPrev() != null)
        {
            m_guiboard.clearMarks();
            undoNode(m_current);
	    m_current = m_current.getPrev();
            playNode(m_current);

            stopClock();            
            refreshGuiForBoardState();
	}
    }

    private void cmdDeleteBranch()
    {
        if (m_current == m_root)
        {
            m_statusbar.setMessage("May not delete root node!");
            System.out.println("May not delete root node!");
            return;
        }

        Node to_be_deleted = m_current;
        backward(1);

        to_be_deleted.removeSelf();
	m_toolbar.updateButtonStates(m_current);
        m_menubar.updateMenuStates(m_current);
    }

    private void determineColorToMove()
    {
	if (m_current == m_root)
        {
            m_tomove = HexColor.BLACK;
        }
        else if (m_current.hasMove())
        {
            // player to move is always opposite of last move
            m_tomove = m_current.getMove().getColor();
            if (m_guiboard.isYBoard() 
                || m_current.getMove().getPoint() != HexPoint.SWAP_PIECES)
                m_tomove = m_tomove.otherColor();
        }
        else if (m_current.hasSetup())
        {
            HexColor color = m_current.getPlayerToMove();
            if (color != null)
                m_tomove = color;
        }
        m_toolbar.setToMove(m_tomove.toString());
    }

    //------------------------------------------------------------

    private void markLastPlayedStone()
    {
        if (m_current == m_root || !m_current.hasMove())
        {
            m_guiboard.markSwapPlayed(null);
	    m_guiboard.markLastPlayed(null);
            return;
        }

        Move move = m_current.getMove();

        if (move.getPoint() == HexPoint.RESIGN)
        {
            m_guiboard.markSwapPlayed(null);
	    m_guiboard.markLastPlayed(null);
            return;
        }

        if (move.getPoint() == HexPoint.SWAP_PIECES)
        {
            Node parent = m_current.getParent();
            assert(parent != null);

            m_guiboard.markLastPlayed(null);
            m_guiboard.markSwapPlayed(parent.getMove().getPoint());
        }
        else
        {
            m_guiboard.markLastPlayed(move.getPoint());
            m_guiboard.markSwapPlayed(null);
        }
    }

    private void setGameChanged(boolean changed)
    {
	m_gameChanged = changed;
    }

    private boolean gameChanged()
    {
	return m_gameChanged;
    }

    private void setFrameTitle()
    {
	String filename = "untitled";
	if (m_file != null) filename = m_file.getName();
	if (gameChanged()) filename = filename + "*";
	String name = "HexGui " + Version.id;
	if (m_white != null)
	    name += " - [" + m_white_name + " " + m_white_version + "]";
	setTitle(name + " - " + filename);
    }

    /** Returns false if action was aborted. */
    private boolean askSaveGame()
    {
	Object options[] = {"Save", "Discard", "Cancel"};
	int n = JOptionPane.showOptionDialog(this,
					     "Game has changed.  Save changes?",
					     "Save Game?",
					     JOptionPane.YES_NO_CANCEL_OPTION,
					     JOptionPane.QUESTION_MESSAGE,
					     null,
					     options,
					     options[0]);
	if (n == 0)
        {
	    if (cmdSaveGame())
                return true;
	    return false;
	}
        else if (n == 1)
        {
	    return true;
	}
	return false;
    }

    /** Saves the current game state as a position in the specified
     * sgf file. */
    private boolean savePosition(File file)
    {
        Node root = new Node();
        Node child = new Node();
        root.addChild(child);

        GameInfo info = new GameInfo();
        info.setBoardSize(m_guiboard.getBoardSize());
        m_guiboard.storePosition(child);
        child.setPlayerToMove(m_tomove);
        return save_tree(file, root, info);
    }

    /** Save game to file.
	@return true If successful.
    */
    private boolean save(File file)
    {
        return save_tree(file, m_root, m_gameinfo);
    }

    private boolean save_tree(File file, Node root, GameInfo gameinfo)
    {
	FileOutputStream out;
	try
        {
	    out = new FileOutputStream(file);
	}
	catch (FileNotFoundException e)
        {
	    ShowError.msg(this, "File not found!");
	    return false;
	}

	new SgfWriter(out, root, gameinfo);
	return true;
    }

    /* Load game from file. */
    private SgfReader load(File file)
    {
	FileInputStream in;
	try
        {
	    in = new FileInputStream(file);
	}
	catch(FileNotFoundException e)
        {
	    ShowError.msg(this, "File not found!");
	    return null;
	}

	SgfReader sgf;
	try
        {
	    sgf = new SgfReader(in);
	}
	catch (SgfReader.SgfError e)
        {
	    ShowError.msg(this, "Error reading SGF file:\n \"" +
                          e.getMessage() + "\"");
	    return null;
	}
	return sgf;
    }

    //------------------------------------------------------------

    /** Show save dialog, return File of selected filename.
	@return null If aborted.
    */
    private File showSaveAsDialog()
    {
	JFileChooser fc = new JFileChooser(m_preferences.get("path-save-game"));
	if (m_file != null) fc.setSelectedFile(m_file);
	int ret = fc.showSaveDialog(this);
	if (ret == JFileChooser.APPROVE_OPTION)
	    return fc.getSelectedFile();
	return null;
    }

    /** Show open dialog, return File of selected filename.
	@return null If aborted.
    */
    private File showOpenDialog()
    {
	JFileChooser fc = new JFileChooser(m_preferences.get("path-load-game"));
	int ret = fc.showOpenDialog(this);
	if (ret == JFileChooser.APPROVE_OPTION)
	    return fc.getSelectedFile();
	return null;
    }

    //------------------------------------------------------------
    
    private void acquireSemaphore()
    {
        try {
            m_semaphore.acquire();
        }
        catch(InterruptedException e) {
            System.out.println("Acquire interrupted!");
        }
    }

    private void releaseSemaphore()
    {
        m_semaphore.release();
    }

    //------------------------------------------------------------

    private void stopClock()
    {
        stopClock(HexColor.BLACK);
        stopClock(HexColor.WHITE);
    }

    private void stopClock(HexColor color)
    {
        if (color == HexColor.BLACK)
            m_blackClock.stop();
        else 
            m_whiteClock.stop();
    }

    private void startClock()
    {
        startClock(m_tomove);
    }

    private void startClock(HexColor color)
    {
        if (color == HexColor.BLACK)
            m_blackClock.start();
        else 
            m_whiteClock.start();
    }

    private void setComment(Node node)
    {
        String comment = node.getComment();
        m_comment.setText(comment);
    }

    public void commentChanged(String string)
    {
        m_current.setComment(string);
    }

    private boolean checkBoardSizeSupported()
    {
        if (m_unsupportedBoardSize)
        {
            ShowError.msg(HexGui.this,
                          format("{0} does not support this board size.",
                                 m_white_name));
            return false;
        }
        return true;
    }

    private void initialize(File file, String command)
    {
        // attach program from the last run of HexGui
        m_program = null;
        if (command != null)
        {
            cmdConnectLocalProgram(new Program("", command, ""));
            setFrameTitle();
        }
        m_programs = Program.load();
        /*
        if (m_preferences.getBoolean("is-program-attached"))
        {
            String name = m_preferences.get("attached-program");
            Program prog = Program.findWithName(name, m_programs);
            if (prog != null)
                cmdConnectLocalProgram(prog);
        }
        */

        if (file != null)
            loadGame(file);
    }

    private void loadGame(File file)
    {
	System.out.println("Loading sgf from file: " + file.getName());
	SgfReader sgf = load(file);
	if (sgf != null)
        {
	    m_root = sgf.getGameTree();
	    m_gameinfo = sgf.getGameInfo();
	    m_current = m_root;

	    m_guiboard.initSize(m_gameinfo.getBoardSize());
            htpBoardsize(m_guiboard.getBoardSize());

	    forward(1000);

	    m_file = file;
	    setGameChanged(false);
	    setFrameTitle();

	    m_preferences.put("path-load-game", file.getPath());
	}
    }

    private void setIcon()
    {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        // There are problems on some platforms with transparency (e.g. Linux
        // Sun Java 1.5.0). Best solution for now is to take an icon without
        // transparency
        URL url = loader.getResource("hexgui/images/hexgui-48x48-notrans.png");
        setIconImage(new ImageIcon(url).getImage());
    }

    private AboutDialog m_about;
    private GuiPreferences m_preferences;
    private GuiBoard m_guiboard;
    private GuiToolBar m_toolbar;
    private StatusBar m_statusbar;
    private GuiMenuBar m_menubar;
    private HtpShell m_shell;
    private AnalyzeDialog m_analyzeDialog;
    private GameInfoPanel m_gameinfopanel;
    private Comment m_comment;
    private boolean m_locked;
    private boolean m_unsupportedBoardSize;
    private Node m_root;
    private Node m_current;
    private GameInfo m_gameinfo;
    private HexColor m_tomove;
    private boolean m_gameChanged;
    private Clock m_blackClock;
    private Clock m_whiteClock;

    private ArrayList<AnalyzeDefinition> m_analyzeCommands;

    private final MessageDialogs m_messageDialogs =
        new MessageDialogs("HexGui");

    private Vector<HexPoint> m_selected_cells;

    private Program m_program;
    private Vector<Program> m_programs;

    private ShowAnalyzeText m_showAnalyzeText;

    private ArrayBlockingQueue<HtpCommand> m_htp_queue;
    private Semaphore m_semaphore;
    private HtpController m_white;
    private String m_white_name;
    private String m_white_version;
    private AnalyzeCommand m_curAnalyzeCommand;
    private Process m_white_process;
    private Socket m_white_socket;

    private File m_file;
}

//----------------------------------------------------------------------------

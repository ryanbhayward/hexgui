//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.htp;

import hexgui.hex.HexPoint;
import hexgui.util.Pair;
import hexgui.util.StringUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.IOException;
import java.util.Vector;

//----------------------------------------------------------------------------

/** Sends HTP commands and parses the response. */
public class HtpController
{ 
    public interface IOInterface
    {
	void sentCommand(String str);
	void receivedResponse(String str);
	void receivedError(String str);
    }

    //------------------------------------------------------------

    public interface GuiFxCallback
    {
        void guifx(String cmd);
    }

    //------------------------------------------------------------

    /** Constructor */
    public HtpController(InputStream in, OutputStream out, 
                         IOInterface io, GuiFxCallback guifx)
    {
	System.out.println("controller: in constructor.");
	m_in = new BufferedReader(new InputStreamReader(in));
	m_out = new PrintStream(out);
	m_io = io;
        m_guifx = guifx;
	m_connected = true;
        m_waiting = false;
    }
    
    public void interrupt()
    {
        System.out.println("Sending interrupt");
        m_out.print("# interrupt\n");
        m_out.flush();
        m_io.sentCommand("# interrupt");
    }

    /** Sends command over the htp channel. 

        Note this method is synchronized, and so HtpController will
        process only a single command at a time.
    */
    public synchronized void sendCommand(String cmd) 
        throws HtpError
    {
	if (!m_connected) 
            return;

	System.out.println("controller: sending '" + cmd.trim() + "'");
	m_out.print(cmd);
	m_out.flush();
	m_io.sentCommand(cmd);
        handleResponse();
    }

    public boolean cmdInProgress() { return m_waiting; }

    public boolean wasSuccess() { return m_success; }

    public String getResponse() { return m_response; }

    private void handleResponse() throws HtpError
    {
        m_waiting = true;

        while (m_waiting) {

            String response;
            try {
                response = waitResponse();
            }
            catch (IOException e) {
                m_waiting = false;
                throw new HtpError("IOException waiting for response!");
            }
            
            //System.out.println("got: '" + response + "'");
            
            if (!m_connected) {
                m_success = false;
                m_response = "";
                m_waiting = false;
                throw new HtpError("Program Disconnected.");
            }
            else if (response == null) {
                m_success = false;
                m_response = "";
                m_waiting = false;
                throw new HtpError("Null response received!");
            } else if (response.length() < 2) {
                m_success = false;
                m_response = response;
                m_waiting = false;
                throw new HtpError("Response length too short! '"+response+"'");
            } else if (response.length() > 10 && 
                       response.substring(0, 10).equals("gogui-gfx:")) 
            {

                String fx 
                    = StringUtils.cleanWhiteSpace(response.substring(10).trim());
                
                m_guifx.guifx(fx);
                
            } else if (response.substring(0,2).equals("= ")) {
                m_success = true;
                m_response = response.substring(2);
                System.out.print("controller: success: ");
                m_io.receivedResponse(response);
                m_waiting = false;
            } else if (response.substring(0,2).equals("? ")) {
                m_success = false;
                m_response = response.substring(2);
                System.out.print("controller: error: "); 
                m_io.receivedError(response);
                m_waiting = false;
            } else {
                m_response = response;
                m_success = false;
                System.out.print("controller: invalid: "); 
                m_waiting = false;
                throw new HtpError("Invalid HTP response:'" + response + "'.");
            }
        }

        System.out.println("'" + m_response.trim() + "'");
    }

    private String waitResponse() throws IOException
    {
	StringBuilder ret = new StringBuilder();
	while (true) {
	    //System.out.println("blocking on response");
	    String line = m_in.readLine();
	    //System.out.println("readline: '" + line + "'");
	    if (line == null) {
		System.out.println("controller: Disconnected!");
		m_connected = false;
		break;
	    }
	    String clean = cleanInput(line);
	    ret.append(clean);
	    ret.append('\n');

	    if (clean.equals(""))
		break;
	}
	System.out.println("controller: done waiting on response.");
	return ret.toString();
    }

    /** Cleans the input.  Removes all occurances of '\r'. 
	Converts all '\t' to ' '. 
    */
    private String cleanInput(String in)
    {
	StringBuilder out = new StringBuilder();
	for (int i=0; i<in.length(); i++) {
	    if (in.charAt(i) == '\t') 
		out.append(' ');
	    else if (in.charAt(i) != '\r') {
		out.append(in.charAt(i));
	    }
	}      
	return out.toString();
    }

    public boolean connected()
    {
        return m_connected;
    }

    private boolean m_connected;
    private BufferedReader m_in;
    private PrintStream m_out;   
    private IOInterface m_io;
    private GuiFxCallback m_guifx;

    private boolean m_waiting;

    private String m_response;
    private boolean m_success;
}

//----------------------------------------------------------------------------

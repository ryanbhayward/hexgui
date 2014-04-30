//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.gui;

import hexgui.util.PrefUtil;

import java.util.Vector;
import java.util.prefs.Preferences;

/** Hex playing Program. */
public class Program
{
    public String m_name;
    public String m_command;
    public String m_working;

    public Program()
    {
    }

    public Program(String name, String command, String working)
    {
        m_name = name;
        m_command = command;
        m_working = working;
    }

    public String toString()
    {
        return m_name;
    }

    //------------------------------------------------------------------------

    public static Vector<Program> load()
    {
        Vector<Program> programs = new Vector<Program>();
        Preferences prefs = PrefUtil.getNode("hexgui/gui/program");
        if (prefs == null)
            return programs;
        int size = prefs.getInt("size", 0);
        for (int i = 0; i < size; ++i)
        {
            prefs = PrefUtil.getNode("hexgui/gui/program/" + i);
            if (prefs == null)
                break;
            String name = prefs.get("name", "");
            String version = prefs.get("version", "");
            String command = prefs.get("command", "");
            String workingDirectory = prefs.get("working-directory", "");
            programs.add(new Program(name, command, workingDirectory));
        }
        return programs;
    }

    public static void save(Vector<Program> programs)
    {
        Preferences prefs = PrefUtil.createNode("hexgui/gui/program");
        if (prefs == null)
            return;
        prefs.putInt("size", programs.size());
        for (int i = 0; i < programs.size(); ++i)
        {
            prefs = PrefUtil.createNode("hexgui/gui/program/" + i);
            if (prefs == null)
                break;
            Program p = programs.get(i);
            prefs.put("name", p.m_name);
            prefs.put("command", p.m_command);
            prefs.put("working-directory", p.m_working);
        }
    }

    public static Program findWithName(String name, Vector<Program> programs)
    {
        for (int i=0; i<programs.size(); ++i) {
            Program p = programs.get(i);
            if (p.m_name.equals(name))
                return p;
        }
        return null;
    }
}

//----------------------------------------------------------------------------

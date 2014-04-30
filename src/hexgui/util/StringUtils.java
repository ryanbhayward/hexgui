package hexgui.util;

import hexgui.hex.HexPoint;
import hexgui.hex.HexColor;
import hexgui.hex.VC;
import hexgui.util.Pair;

import java.io.StringReader;
import java.io.PrintStream;
import java.io.IOException;
import java.util.Vector;
import java.util.ArrayList;

public final class StringUtils
{
    /** Capitalize the first word and trim whitespaces. */
    public static String capitalize(String message)
    {
        message = message.trim();
        if (message.equals(""))
            return message;
        StringBuilder buffer = new StringBuilder(message);
        char first = buffer.charAt(0);
        if (! Character.isUpperCase(first))
            buffer.setCharAt(0, Character.toUpperCase(first));
        return buffer.toString();
    }

    /** Check if string is null, empty, or contains only whitespaces. */
    public static boolean isEmpty(String s)
    {
        if (s == null)
            return true;
        for (int i = 0; i < s.length(); ++i)
            if (! Character.isWhitespace(s.charAt(i)))
                return false;
        return true;
    }

    /** Converts all whitespace characters to a single ' '. */
    public static String cleanWhiteSpace(String str)
    {
        StringReader reader = new StringReader(str);
        StringBuilder ret = new StringBuilder();

        boolean white = false;
        while (true) {
            int c;
            try {
                c = reader.read();
            }
            catch (Throwable t) {
                System.out.println("Something bad happened!");
                break;
            }

            if (c == -1) break;
            if (c == ' ' || c == '\n' || c == '\t') {
                if (!white) ret.append(" ");
                white = true;
            } else {
                white = false;
                ret.append((char)c);
            }
        }
        return ret.toString();
    }

    public static Vector<HexPoint> parsePointList(String str, String sep)
    {
	Vector<HexPoint> ret = new Vector<HexPoint>();
        String cleaned = cleanWhiteSpace(str.trim());
        if (cleaned.length() == 0)
            return ret;

	String[] pts = cleaned.split(sep);
	for (int i=0; i<pts.length; i++) {
	    HexPoint p = HexPoint.get(pts[i].trim());
	    ret.add(p);
	}
	return ret;
    }

    public static Vector<Pair<HexColor, HexPoint> > parseVariation(String str)
    {
        Vector<Pair<HexColor, HexPoint> > ret 
            =  new Vector<Pair<HexColor, HexPoint> >(); 

        Vector<Pair<String, String> > pairs 
            = StringUtils.parseStringPairList(str.trim());
        for (int i=0; i<pairs.size(); ++i) 
        {
            HexColor color = (pairs.get(i).first.charAt(0) == 'B') 
                ? HexColor.BLACK : HexColor.WHITE;
            HexPoint point = HexPoint.get(pairs.get(i).second);
            ret.add(new Pair<HexColor, HexPoint>(color, point));
        }
        return ret;
    }

    public static Vector<HexPoint> parsePointList(String str)
    {
        return parsePointList(str, " ");
    }

    public static Vector<String> parseStringList(String str)
    {
	Vector<String> ret = new Vector<String>();
        String cleaned = cleanWhiteSpace(str.trim());
        if (cleaned.length() == 0)
            return ret;

	String[] strs = cleaned.split(" ");
	for (int i=0; i<strs.length; i++) {
	    String cur = strs[i].trim();
	    ret.add(cur);
	}
	return ret;
    }

    public static Vector<Pair<String, String> > parseStringPairList(String str)
    {
	Vector<Pair<String, String> > ret = new Vector<Pair<String, String> >();
        String cleaned = cleanWhiteSpace(str.trim());
        if (cleaned.length() == 0)
            return ret;

	String[] strs = cleaned.split(" ");
	for (int i=0; i<strs.length; i+=2) {
	    String c1 = strs[i].trim();
            String c2 = strs[i+1].trim();
	    ret.add(new Pair<String, String>(c1, c2));
	}
	return ret;
    }

    public static Vector<VC> parseVCList(String str)
    {
        Vector<VC> ret = new Vector<VC>();
        String cleaned = cleanWhiteSpace(str.trim());
        if (cleaned.length() == 0)
            return ret;

	String[] vcs = cleaned.split(" ");

        for (int i=0, j=0; i<vcs.length; i+=j) {
            HexPoint from, to;
            HexColor color;
            String type = "unknown";
            int moves = 0;
            Vector<HexPoint> carrier = new Vector<HexPoint>();
            Vector<HexPoint> stones = new Vector<HexPoint>();
            Vector<HexPoint> key = new Vector<HexPoint>();
            String source = "unknown";

            try {
                color = HexColor.get(vcs[i+0]);
                from = HexPoint.get(vcs[i+1]);
                to = HexPoint.get(vcs[i+2]);
                type = vcs[i+3];

                j = 5;
                if (!type.equals("softlimit")) {
                    source = vcs[i+4];
  
                    // read carrier set
                    if (!vcs[i+5].equals("["))
                        throw new Throwable("No carrier!");
                    
                    for (j=6; j < vcs.length; j++) {
                        if (vcs[i+j].equals("]")) break;
                        HexPoint p = HexPoint.get(vcs[i+j]);
                        carrier.add(p);
                    }
                    
                    j++;  // skip closing ']'
                
                    // read stone set
                    if (!vcs[i+j].equals("["))
                        throw new Throwable("No stones! Should be '[', got '" +
                                            vcs[j] + "'");
                    
                    for (j++; j < vcs.length; j++) {
                        if (vcs[i+j].equals("]")) break;
                        HexPoint p = HexPoint.get(vcs[i+j]);
                        stones.add(p);
                    }
                    
                    j++;  // skip closing ']'

                    int blah = 0;
                    if (type.equals("semi")) blah = 1;
                    for (int k=0; k<blah; k++, j++) {
                        HexPoint p = HexPoint.get(vcs[i+j]);
                        key.add(p);
                    }
                }

            }
            catch(Throwable t) {
                System.out.println("Exception occured while parsing VC: '" +
                                   t.getMessage() + "'");
                return ret;                
            }

            ret.add(new VC(from, to, color, type, 
                           source, moves, carrier, stones, key));
        }
        return ret;
    }

    public static String reverse(String str)
    {
        StringBuilder ret = new StringBuilder();
        for (int i=str.length()-1; i>=0; i--) {
            ret.append(str.charAt(i));
        }
        return ret.toString();
    }

    /** Split command line into arguments.
        Allows " for words containing whitespaces.
    */
    public static String[] splitArguments(String string)
    {
        assert string != null;
        ArrayList<String> result = new ArrayList<String>();
        boolean escape = false;
        boolean inString = false;
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < string.length(); ++i)
        {
            char c = string.charAt(i);
            if (c == '"' && ! escape)
            {
                if (inString)
                {
                    result.add(token.toString());
                    token.setLength(0);
                }
                inString = ! inString;
            }
            else if (Character.isWhitespace(c) && ! inString)
            {
                if (token.length() > 0)
                {
                    result.add(token.toString());
                    token.setLength(0);
                }
            }
            else
                token.append(c);
            escape = (c == '\\' && ! escape);
        }
        if (token.length() > 0)
            result.add(token.toString());
        return result.toArray(new String[result.size()]);
    }

    /** Return a printable error message for an exception.
        Returns the error message is for instances of ErrorMessage or
        for other exceptions the class name with the exception message
        appended, if not empty. */
    public static String getErrorMessage(Throwable e)
    {
        String message = e.getMessage();
        boolean hasMessage = ! StringUtils.isEmpty(message);
        String className = e.getClass().getName();
        String result;
        if (e instanceof ErrorMessage)
            result = message;
        else if (hasMessage)
            result = className + ":\n" + message;
        else
            result = className;
        return result;
    }

    /** Print exception to standard error.
        Prints the class name and message to standard error.
        For exceptions of type Error or RuntimeException, a stack trace
        is printed in addition.
        @return A slightly differently formatted error message
        for display in an error dialog. */
    public static String printException(Throwable exception)
    {
        String result = getErrorMessage(exception);
        System.err.println(result);
        boolean isSevere = (exception instanceof RuntimeException
                            || exception instanceof Error);
        if (isSevere)
            exception.printStackTrace();
        return result;
    }

}

//----------------------------------------------------------------------------

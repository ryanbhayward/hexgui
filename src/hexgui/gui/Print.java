//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------
package hexgui.gui;

import java.awt.Component;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

/** Print a printable. */
public final class Print
{
    public static void run(Component parent, Printable printable)
    {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(printable);
        if (! job.printDialog())
            return;
        try
        {
            job.print();
        }
        catch (Exception e)
        {
            System.out.println("Printing failed!");
        }
    }

    /** Make constructor unavailable; class is for namespace only. */
    private Print()
    {
    }
}

//----------------------------------------------------------------------------

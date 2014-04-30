//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

// 
// The basis of this was stolen from the thread:
//  http://forum.java.sun.com/thread.jspa?threadID=601884&messageID=4215335
//

package hexgui.gui;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.image.*;
 
public class PrintPreview 
    extends JPanel implements ActionListener
{
  static final double INITIAL_SCALE_DIVISOR = 1.0; // scale factor
 
  Component targetComponent;
  PageFormat pageFormat = new PageFormat();
  double scaleDivisor = INITIAL_SCALE_DIVISOR;
  BufferedImage pcImage;
 
  JPanel hold = new JPanel();
  PreviewPage prp;
 
  ButtonGroup pf = new ButtonGroup();
  JRadioButton pf1;
  JRadioButton pf2;
  JLabel xsl = new JLabel("Scale:", JLabel.LEFT);
  JButton preview = new JButton("PREVIEW");
  JButton print = new JButton("PRINT");
 
  JSpinner xsp;
  SpinnerNumberModel snmx;
  
  JFrame workFrame;
  
  Color bgColor = Color.white;
 
  int pcw, pch;
  double wh, hw;
  
  public PrintPreview(Component pc){
    setBackground(bgColor);
 
    targetComponent = pc;
 
    // for a JTable, we can't use simple component.paint(g) call
    // because it doesn't paint table header !!
    if (pc instanceof JTable){
      TableModel tm = ((JTable)pc).getModel();
      JTable workTable = new JTable(tm); // make pure clone
      targetComponent = getTableComponent(workTable);
    }
 
    pcImage = new BufferedImage(pcw = targetComponent.getWidth(),
     pch = targetComponent.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics g = pcImage.createGraphics();
    targetComponent.paint(g);
    g.dispose();
    wh = (double)pcw / (double)pch;
    hw = (double)pch / (double)pcw;
    /* workFrame is used in getTableComponent() method
     * only for visualizing the table component and its header
     */
    if (workFrame != null){ // if you don't use table clone here,
      workFrame.dispose();  // calling dispose() delete the table
    }                       // from original app window
 
    pageFormat.setOrientation(PageFormat.LANDSCAPE);
    prp = new PreviewPage();
 
    snmx = new SpinnerNumberModel(INITIAL_SCALE_DIVISOR, 1.0, 10.0, 0.1);
    xsp = new JSpinner(snmx);
    
    pf2 = new JRadioButton("Landscape");
    pf2.setActionCommand("2");
    pf2.setSelected(true);
    pf1 = new JRadioButton("Portrait");
    pf1.setActionCommand("1");
    pf1.setSelected(false);
    pf.add(pf2);
    pf.add(pf1);
    pf2.setBackground(bgColor);
    pf1.setBackground(bgColor);
 
    preview.addActionListener(this);
    print.addActionListener(this);
 
    prp.setBackground(bgColor);
    hold.setBorder(BorderFactory.createLineBorder(Color.black, 2));
    hold.setBackground(bgColor);
    hold.setLayout(new GridBagLayout());
 
    GridBagConstraints c1 = new GridBagConstraints();
 
    c1.insets = new Insets(15, 45, 0, 5);
    c1 = buildConstraints(c1, 0, 0, 2, 1, 0.0, 0.0);
    hold.add(pf1, c1);
 
    c1.insets = new Insets(2, 45, 0, 5);
    c1 = buildConstraints(c1, 0, 1, 2, 1, 0.0, 0.0);
    hold.add(pf2, c1);
 
    c1.insets = new Insets(25, 5, 0, 5);
    c1 = buildConstraints(c1, 0, 2, 1, 1, 0.0, 0.0);
    hold.add(xsl, c1);
 
    c1.insets = new Insets(25, 5, 0, 35);
    c1 = buildConstraints(c1, 1, 2, 1, 1, 0.0, 0.0);
    hold.add(xsp, c1);
 
    c1.insets = new Insets(25, 35, 0, 35);
    c1 = buildConstraints(c1, 0, 6, 2, 1, 0.0, 0.0);
    hold.add(preview, c1);
 
    c1.insets = new Insets(5, 35, 25, 35);
    c1 = buildConstraints(c1, 0, 7, 2, 1, 0.0, 0.0);
    hold.add(print, c1);
 
    add(hold);
    add(prp);
  }
 
  Component getTableComponent(JTable table){
    Box box = new Box(BoxLayout.Y_AXIS);
    JTableHeader jth = table.getTableHeader();
 
    Dimension dh = jth.getPreferredSize();
    Dimension dt = table.getPreferredSize();
    Dimension db = new Dimension(dh.width, dh.height + dt.height);
    box.setPreferredSize(db);
 
    jth.setBorder(new LineBorder(Color.black, 1){
      public Insets getBorderInsets(Component c){
        return new Insets(2, 2, 2, 2);
      }
    });
 
    table.setBorder(new PartialLineBorder(false, true, false, false));
 
    box.add(jth);
    box.add(table);
 
    // visualize table for getting non-zero sizes(width, height)
    workFrame = new JFrame();
    workFrame.getContentPane().add(box);
    workFrame.pack();
    workFrame.setVisible(true);
 
    return box;
  }
 
  GridBagConstraints buildConstraints(GridBagConstraints gbc, int gx, int gy,
   int gw, int gh, double wx, double wy){
    gbc.gridx = gx;
    gbc.gridy = gy;
    gbc.gridwidth = gw;
    gbc.gridheight = gh;
    gbc.weightx = wx;
    gbc.weighty = wy;
    gbc.fill = GridBagConstraints.BOTH;
    return gbc;
  }
 
  public class PreviewPage extends JPanel{
    int x1, y1, l1, h1, x2, y2;
    Image image;
    
    public PreviewPage(){
      setPreferredSize(new Dimension(460, 460));
      setBorder(BorderFactory.createLineBorder(Color.black, 2));
    }
 
    public void paintComponent(Graphics g){
      super.paintComponent(g);
 
      // PORTRAIT
      if(pageFormat.getOrientation() == PageFormat.PORTRAIT){
        g.setColor(Color.black);
        g.drawRect(60, 10, 340, 440);
        x1 = (int)Math.rint(((double)pageFormat.getImageableX() / 72) * 40);
        y1 = (int)Math.rint(((double)pageFormat.getImageableY() / 72) * 40);
        l1 
         = (int)Math.rint(((double)pageFormat.getImageableWidth() / 72) * 40);
        h1 
         = (int)Math.rint(((double)pageFormat.getImageableHeight() / 72) * 40);
        g.setColor(Color.red);
        g.drawRect(x1 + 60, y1 + 10, l1, h1);
        // setScales(); // commenting-out suppresses too frequent paint updates
        x2 = (int)Math.rint((double)l1 / scaleDivisor);
        y2 = (int)Math.rint(((double)l1 * hw) / scaleDivisor);
        image = pcImage.getScaledInstance(x2, y2, Image.SCALE_AREA_AVERAGING);

        int xspace = (l1 - x2) / 2;
        int yspace = (h1 - y2) / 2;
        g.drawImage(image, x1 + 60 + xspace, y1 + 10 + yspace, this);
      }
      // LANDSCAPE
      else{
        g.setColor(Color.black);
        g.drawRect(10, 60, 440, 340);
        x1 = (int)Math.rint(((double)pageFormat.getImageableX() / 72) * 40);
        y1 = (int)Math.rint(((double)pageFormat.getImageableY() / 72) * 40);
        l1 
         = (int)Math.rint(((double)pageFormat.getImageableWidth() / 72) * 40);
        h1 
         = (int)Math.rint(((double)pageFormat.getImageableHeight() / 72) * 40);
        g.setColor(Color.red);
        g.drawRect(x1 + 10, y1 + 60, l1, h1);
        // setScales();
        x2 = (int)Math.rint((double)l1 / scaleDivisor);
        y2 = (int)Math.rint(((double)l1 * hw) / scaleDivisor);
        image = pcImage.getScaledInstance(x2, y2, Image.SCALE_AREA_AVERAGING);

        int xspace = (l1 - x2) / 2;
        int yspace = (h1 - y2) / 2;
        g.drawImage(image, x1 + 10 + xspace, y1 + 60 + yspace, this);
      }
    } 
  }
 
  public void actionPerformed(ActionEvent e){
    if(e.getSource() == preview){
      setProperties();
    }
    if(e.getSource() == print){
      doPrint();
    }
  }
 
  public void setProperties(){
    if(pf1.isSelected()){
      pageFormat.setOrientation(PageFormat.PORTRAIT);
    }
    else if(pf2.isSelected()){
      pageFormat.setOrientation(PageFormat.LANDSCAPE);
    }
    setScales();
    prp.repaint();
  }
 
  public void setScales(){
    try{
        scaleDivisor = ((Double)xsp.getValue()).doubleValue();
    }
    catch (NumberFormatException e) {
    }
  }
 
  public void doPrint(){
    PrintThis();
  }
 
  public void PrintThis(){
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    Book book = new Book();
    book.append(new PrintPage(), pageFormat);
    printerJob.setPageable(book);
    boolean doPrint = printerJob.printDialog();
    if (doPrint) {
      try {
        printerJob.print();
      }
      catch (PrinterException exception) {
        System.err.println("Printing error: " + exception);
      }
    }
  }
 
  //public class PrintPage implements Printable{
  class PrintPage implements Printable{
 
    public int print(Graphics g, PageFormat format, int pageIndex) {
      Graphics2D g2D = (Graphics2D) g;
      g2D.translate(format.getImageableX (), format.getImageableY ());
//      disableDoubleBuffering(mp);
      System.out.println("get i x " + format.getImageableX ());
      System.out.println("get i x " + format.getImageableY ());
      System.out.println("getx: " + format.getImageableWidth() );
      System.out.println("getx: " + format.getImageableHeight() );
      // scale to fill the page
      double dw = format.getImageableWidth();
      double dh = format.getImageableHeight();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      setScales();
      double xScale = dw / (1028 / scaleDivisor);
      double yScale = dh / (768 / scaleDivisor);
      double scale = Math.min(xScale, yScale);
      System.out.println("" + scale);
      g2D.scale( xScale, yScale);
      targetComponent.paint(g);
//      enableDoubleBuffering(mp);
      return Printable.PAGE_EXISTS;
    }
 
    public void disableDoubleBuffering(Component c) {
      RepaintManager currentManager = RepaintManager.currentManager(c);
      currentManager.setDoubleBufferingEnabled(false);
    }
 
    public void enableDoubleBuffering(Component c) {
      RepaintManager currentManager = RepaintManager.currentManager(c);
      currentManager.setDoubleBufferingEnabled(true);
    }
  }

}
 
class PartialLineBorder extends AbstractBorder{
  boolean top, left, bottom, right;
 
  public PartialLineBorder(boolean t, boolean l, boolean b, boolean r){
    top = t;
    left = l;
    bottom = b;
    right = r;
  }
 
  public boolean isBorderOpaque(){
    return true;
  }
 
  public Insets getBorderInsets(Component c){
    return new Insets(2, 2, 2, 2);
  }
  
  public void paintBorder
   (Component c, Graphics g, int x, int y, int width, int height){
 
    Graphics2D g2 = (Graphics2D)g;
    g2.setStroke(new BasicStroke(1.0f));
 
    if (top){
      g2.drawLine(x, y, x + width, y);
    }
    if (left){
      g2.drawLine(x, y, x, y + height);
    }
    if (bottom){
      g2.drawLine(x, y + height, x + width, y + height);
    }
    if (right){
      g2.drawLine(x + width, y, x + width, y + height);
    }
  }
}

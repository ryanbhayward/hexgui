//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package hexgui.util;

import java.awt.*;

//----------------------------------------------------------------------------

/** Creates hexagons. */
public final class Hexagon
{

    /** Creates a hexagon with flat sides centered at the specified point.
	@param p center of hexagaon.
	@param width the width of the hexagon.
	@param height the height of the hexagon.
	@return an instance of Polygon with the above properties.
    */
    public static Polygon 
	createVerticalHexagon(Point p, int width, int height)
    {
	int xpoints[] = new int[6];
	int ypoints[] = new int[6];

	int sx = width/2;
	int ly = height/2;
        int sy = ly/2;
	
	xpoints[0] = 0;   ypoints[0] = -ly;
	xpoints[1] = -sx; ypoints[1] = -sy;
	xpoints[2] = -sx; ypoints[2] = +sy;
	xpoints[3] = 0;   ypoints[3] = +ly;
	xpoints[4] = +sx; ypoints[4] = +sy;
	xpoints[5] = +sx; ypoints[5] = -sy;

	Polygon ret = new Polygon(xpoints, ypoints, 6);
	ret.translate(p.x, p.y);

	return ret;
    }

    /** Creates a hexagon with flat top and bottom centered at 
	the specified point.
	@param p center of hexagaon.
	@param width the width of the hexagon.
	@param height the height of the hexagon.
	@return an instance of Polygon with the above properties.
    */
    public static Polygon 
	createHorizontalHexagon(Point p, int width, int height)
    {
	int xpoints[] = new int[6];
	int ypoints[] = new int[6];

	int sy = height/2;
	int lx = width/2;
	int sx = lx/2;

	xpoints[0] = -lx; ypoints[0] = 0;
	xpoints[1] = -sx; ypoints[1] = +sy;
	xpoints[2] = +sx; ypoints[2] = +sy;
	xpoints[3] = +lx; ypoints[3] = 0;
	xpoints[4] = +sx; ypoints[4] = -sy;
	xpoints[5] = -sx; ypoints[5] = -sy;

	Polygon ret = new Polygon(xpoints, ypoints, 6);
	ret.translate(p.x, p.y);

	return ret;
    }

    /** Private constructor. */
    private Hexagon() {}
}

//----------------------------------------------------------------------------

// ConstPointList.java

package hexgui.hex;

import java.util.Iterator;

/** Const functions of go.PointList.
    @see PointList */
public interface ConstPointList
    extends Iterable<HexPoint>
{
    boolean contains(Object elem);

    boolean equals(Object object);

    HexPoint get(int index);

    int hashCode();

    boolean isEmpty();

    Iterator<HexPoint> iterator();

    int size();

    String toString();
}

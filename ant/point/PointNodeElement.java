package ant.point;

import java.awt.Point;

import ant.AbstractNodeElement;

@SuppressWarnings("serial")
public class PointNodeElement<T> extends AbstractNodeElement<T> {

	public PointNodeElement(Point coordinates, T element) {
		super(coordinates, element);
	}

}

package ant;

import java.awt.Point;
import java.util.ArrayList;

import akka.actor.ActorRef;

public class VisionRequest {
	public int id;
	public Point center;
	public ArrayList<ActorRef> patches = null;
}

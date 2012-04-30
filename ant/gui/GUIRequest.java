package ant.gui;

import java.util.TreeSet;

import akka.actor.ActorRef;

public class GUIRequest {
	final int x;
	final int y;
	final TreeSet<ActorRef> patches;
	
	public GUIRequest(int xdim, int ydim, TreeSet<ActorRef> p){
		x = xdim;
		y = ydim;
		patches = p;
	}
}

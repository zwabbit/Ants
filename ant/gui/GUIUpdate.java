package ant.gui;

import java.util.TreeSet;

import akka.actor.ActorRef;
import ant.GetPatchInfo;

public class GUIUpdate {
	public final GetPatchInfo info;
	public GUIUpdate(GetPatchInfo rq){
		info = rq;
	}
	public GUIUpdate() {
		// TODO Auto-generated constructor stub
		info = null;
	}
}

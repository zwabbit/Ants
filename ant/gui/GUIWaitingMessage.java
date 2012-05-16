package ant.gui;

import akka.actor.ActorRef;

public class GUIWaitingMessage {
	public final boolean doContinue;
	public final ActorRef ant;
	public GUIWaitingMessage(boolean b, ActorRef a){
		ant = a;
		doContinue = b;
	}
	public GUIWaitingMessage() {
		// TODO Auto-generated constructor stub
		ant = null;
		doContinue = false;
	}
}

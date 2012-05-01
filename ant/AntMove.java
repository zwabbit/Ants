package ant;

import akka.actor.ActorRef;

public class AntMove {
	public ActorRef movePatch;
	public boolean go;
	public AntMove(){
		
	}
	public AntMove(boolean keepgoing){
		go = keepgoing;
	}
}

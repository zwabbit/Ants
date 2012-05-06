package ant;

import java.util.HashMap;

import scala.concurrent.stm.Ref.View;
import scala.concurrent.stm.*;
import akka.actor.ActorRef;

public class GetPatchInfo {
	public final int x;
	public final int y;
	public final Integer food;
	public final float pher;
	public final Integer ants;
	public final ActorRef assocAnt;
	
	public GetPatchInfo(){
		y=-1;
		x=-1;
		ants = null;
		food=null;
		pher=-1;
		assocAnt = null;
	}
	
	public GetPatchInfo(int xi, int yi, Integer food2, float p, Integer ants2){
		x = xi;
		y = yi;
		food = food2;
		pher = p;
		ants = ants2;
		assocAnt = null;
	}
	public GetPatchInfo(int xi, int yi, Integer food2, float p, Integer ants2, ActorRef ant){
		x = xi;
		y = yi;
		food = food2;
		pher = p;
		ants = ants2;
		assocAnt = ant;
	}
}

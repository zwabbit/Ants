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
	public final TMap.View<Integer, ActorRef> ants;
	
	public GetPatchInfo(){
		y=-1;
		x=-1;
		ants = null;
		food=null;
		pher=-1;
	}
	
	public GetPatchInfo(int xi, int yi, View<Integer> food2, float p, TMap.View<Integer, ActorRef> ants2){
		x = xi;
		y = yi;
		food = food2.get();
		pher = p;
		ants = ants2;
	}
}

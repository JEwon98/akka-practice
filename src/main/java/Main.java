import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import akka.actor.Props;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Main {
    public static void main(String[] args) {
        // ActorSystem 생성
        ActorSystem<MasterActor.Command> system = ActorSystem.create(MasterActor.create(),"master");

        // 액터 생성
        // 1부터 1억까지의 수를 액터에게 보내고 결과를 받음
        system.tell(new MasterActor.GetPrimenumber(150_000_000));
        // 시스템 종료
//        system.terminate();
    }
}

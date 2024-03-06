import akka.actor.Actor;
import akka.actor.SupervisorStrategy;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.ArrayList;

// 마스터 액터 클래스 정의
class MasterActor extends AbstractBehavior<MasterActor.Command> {
    private final int numWorkers = Runtime.getRuntime().availableProcessors(); // CPU 코어 수만큼 워커 액터 생성
    private final ArrayList<ActorRef<Command>> workers;
    private int primeNumberCount;
    private long beforeTime;
    private long afterTime;
    public MasterActor(ActorContext<Command> context) {
        super(context);
        this.workers = new ArrayList<>();
        this.primeNumberCount = 0;
    }

    interface Command {}

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> new MasterActor(context));
    }
    public static class GetPrimenumber implements Command {
        public final int number;
        public GetPrimenumber(int number) {
            this.number = number;
        }
    }
    public static class Num implements Command{
        private int number;
        private ActorRef<Command> replyTo;
        public Num(int i, ActorRef<Command> self) {
            this.number = i;
            this.replyTo = self;
        }
    }
    public static class AddPrimenumber implements Command {}

    public static class ChildStopped implements Command {
        private ActorRef<Command> sender;
        public ChildStopped(ActorRef<Command> sender) {
            this.sender = sender;
        }
    }
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetPrimenumber.class, this::onGetPrimenumber)
                .onMessage(AddPrimenumber.class, this::onAddPrimenumber)
                .onMessage(ChildStopped.class, this::onChildStopped)
                .build();
    }



    private Behavior<Command> onGetPrimenumber(GetPrimenumber getPrimenumber) {
        System.out.println("numWorkers = " + numWorkers);
        // start timer
        beforeTime= System.currentTimeMillis();

        for(int i=0;i<numWorkers;i++){
            workers.add(getContext().spawn(PrimeCheckerActor.create(),"worker_"+i));
        }
        ActorRef<Command> replyTo = getContext().getSelf();
        for(int i=2;i<=getPrimenumber.number;i++){
            int workerIndex = i%numWorkers;
            workers.get(workerIndex).tell(new PrimeCheckerActor.ReturnNum(i,replyTo));
        }
        for(ActorRef<Command> worker : workers){
            worker.tell(new PrimeCheckerActor.StopWork(replyTo));
        }
        return this;
    }

    private Behavior<Command> onAddPrimenumber(AddPrimenumber addPrimenumber) {
        primeNumberCount+=1;
        return this;
    }
    private Behavior<Command> onChildStopped(ChildStopped childStopped) {
        workers.remove(childStopped.sender);
        if(workers.isEmpty()){
            afterTime = System.currentTimeMillis();
            System.out.println("primeNumberCount = " + primeNumberCount);
            System.out.println("time diff = " + (afterTime - beforeTime));
            return null;
        }
        return this;
    }
}

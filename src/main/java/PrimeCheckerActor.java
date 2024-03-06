import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class PrimeCheckerActor extends AbstractBehavior<MasterActor.Command> {

    public PrimeCheckerActor(ActorContext<MasterActor.Command> context) {
        super(context);
    }

    public static Behavior<MasterActor.Command> create() {
        return Behaviors.setup(context -> new PrimeCheckerActor(context));
    }

    public static class ReturnNum implements MasterActor.Command {
        private int number;
        private ActorRef<MasterActor.Command> replyTo;

        public ReturnNum(int number, ActorRef<MasterActor.Command> replyTo) {
            this.number = number;
            this.replyTo = replyTo;
        }
    }
    public static class StopWork implements MasterActor.Command {
        private ActorRef<MasterActor.Command> replyTo;

        public StopWork(ActorRef<MasterActor.Command> replyTo) {
            this.replyTo = replyTo;
        }
    }
    @Override
    public Receive<MasterActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReturnNum.class,this::onReturnNum)
                .onMessage(StopWork.class,this::onStopWork)
                .build();
//        return receiveBuilder()
//                .match(Integer.class, number -> {
//                    // 소수인지 여부를 판단하고 결과를 부모 액터에게 보냄
//                    Boolean isPrime = isPrime(number);
////                    if(isPrime)
////                        System.out.println(number + ", isPrime : " + isPrime);
////                    System.out.println("getSender() = " + getSender());
//                    getSender().tell(isPrime, getSelf());
//                })
//                .build();
    }

    private Behavior<MasterActor.Command> onStopWork(StopWork stopWork) {
        stopWork.replyTo.tell(new MasterActor.ChildStopped(getContext().getSelf()));
        getContext().stop(getContext().getSelf());
        return null;
    }

    private Behavior<MasterActor.Command> onReturnNum(ReturnNum returnNum) {
        if(isPrime(returnNum.number)){
            returnNum.replyTo.tell(new MasterActor.AddPrimenumber());
        }
        return this;
    }

    // 소수 판별 메서드
    private boolean isPrime(int number) {
        if (number <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }
}
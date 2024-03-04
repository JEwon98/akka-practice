import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

// 액터 클래스 정의
class PrimeCheckerActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Integer.class, number -> {
                    // 소수인지 여부를 판단하고 결과를 부모 액터에게 보냄
                    Boolean isPrime = isPrime(number);
//                    if(isPrime)
//                        System.out.println(number + ", isPrime : " + isPrime);
//                    System.out.println("getSender() = " + getSender());
                    getSender().tell(isPrime, getSelf());
                })
                .build();
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

public class Main {
    public static void main(String[] args) {
        // ActorSystem 생성
        ActorSystem system = ActorSystem.create("PrimeCheckerSystem");

        // 액터 생성
        ActorRef master = system.actorOf(Props.create(MasterActor.class), "master");
        long beforeTime = System.currentTimeMillis();
        // 1부터 1억까지의 수를 액터에게 보내고 결과를 받음
        master.tell(1_000_000_000, ActorRef.noSender());
        // 시스템 종료
        system.terminate();
        long afterTime = System.currentTimeMillis();
        long secDiffTime = (afterTime - beforeTime); //두 시간에 차 계산
        System.out.println("시간차이(m) : "+secDiffTime);
    }
}

// 마스터 액터 클래스 정의
class MasterActor extends AbstractActor {
    private final int numWorkers = Runtime.getRuntime().availableProcessors(); // CPU 코어 수만큼 워커 액터 생성

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Integer.class, maxNumber -> {
                    // 워커 액터 생성
                    for (int i = 0; i < numWorkers; i++) {
                        getContext().actorOf(Props.create(PrimeCheckerActor.class), "worker_" + i);
                        System.out.println("worker_"+i );
                    }

                    // 각 워커에 작업 분배
                    for (int number = 2; number <= maxNumber; number++) {
                        int workerIndex = number % numWorkers;
                        getContext().findChild("worker_" + workerIndex).get().tell(number, getSelf());
                    }
                })
                .build();
    }
}

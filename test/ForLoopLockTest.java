import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mati on 05/07/2014.
 */
public class ForLoopLockTest {

    private final static List<Integer> list = ImmutableList.<Integer>builder().add(1).add(2).add(3).build();
    private final static ReentrantLock lock = new ReentrantLock();

    public static void main(final String[] args) throws Exception {
        Stopwatch stopwatch = new Stopwatch().start();
        for (int i = 0; i<10000; i++) {
            Locks.withLock(lock, () -> list.get(0));
            //list.get(0);
        }
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    //1.0000 = 41ms
    //10.000 = 45ms

    //1.000.000 = 70-80
    //1.000.000 - without lock = 7-8ms = 10 times slower

}

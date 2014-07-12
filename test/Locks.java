import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

/**
 * Created by mati on 05/07/2014.
 */
public class Locks {

    public static <T> T withLock(final Lock lock, final Callable<T> callable) {
        try {
            lock.lock();
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

}

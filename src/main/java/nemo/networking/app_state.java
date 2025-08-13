package nemo.networking;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class app_state {
    private static boolean app_stet_r = true;

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    public static boolean isApp_stet_r() {
        lock.readLock().lock();
        try {
            return app_stet_r;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static void setApp_stet_r(boolean value) {
        lock.writeLock().lock();
        try {
            app_stet_r = value;
        } finally {
            lock.writeLock().unlock();
        }
    }
}

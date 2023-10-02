package com.github.starnowski.concurrency.fun.java21;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RateLimiterImpl implements RateLimiter {

    private final Clock clock;
    private final int maxLimit;
    private final Duration slicePeriod;

    /**
     * For tests purpose
     * @return
     */
    ConcurrentHashMap<Key, WorkUnit> getMap() {
        return map;
    }

    private final ConcurrentHashMap<Key, WorkUnit> map = new ConcurrentHashMap<>();

    public RateLimiterImpl(Clock clock) {
        this(clock, 5, Duration.ofSeconds(10 * 60));
    }

    public RateLimiterImpl(Clock clock, int maxLimit, Duration slicePeriod) {
        this.clock = clock;
        this.maxLimit = maxLimit;
        this.slicePeriod = slicePeriod;
    }

    @Override
    public boolean canAccept(String userAgent, String ipAddress) {
        Key key = prepareKey(userAgent, ipAddress);
        Instant instant = this.clock.instant();
        Instant beginningOfSlice = instant.minus(this.slicePeriod);
        WorkUnit workUnit = map.computeIfAbsent(key, (k) -> new WorkUnit());
        return workUnit.tryRegisterRequestWhenCanBeAccepted(instant, beginningOfSlice, maxLimit);
    }

    public void cleanOldWorkUnits()
    {
        Instant instant = this.clock.instant();
        Instant beginningOfSlice = instant.minus(this.slicePeriod);
        List<Key> keysToBeDeleted = new ArrayList<>();
        for (Map.Entry<Key, WorkUnit> entry : map.entrySet()) {
            long numberOfValidRequests = entry.getValue().getRequestInstants().stream().filter(instant1 -> instant1.isAfter(beginningOfSlice)).count();
            if (numberOfValidRequests == 0) {
                keysToBeDeleted.add(entry.getKey());
            }
        }
        for (Key key: keysToBeDeleted) {
            map.remove(key);
        }
    }

    static Key prepareKey(String userAgent, String ipAddress) {
        return new Key(userAgent, ipAddress);
    }

    static class WorkUnit {

        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        List<Instant> getRequestInstants() {
            return requestInstants;
        }

        private final List<Instant> requestInstants = new CopyOnWriteArrayList<>();

        public boolean tryRegisterRequestWhenCanBeAccepted(Instant instant, Instant beginningOfSlice, int maxLimit) {
            //Fail fast
            long numberOfAcceptedRequestsX = requestInstants.stream().filter(instant1 -> instant1.isAfter(beginningOfSlice)).count();
            if (!(numberOfAcceptedRequestsX < maxLimit))
                return false;
            boolean lockAcquired = false;
            try {
                lockAcquired = tryAcquireLock(3);
                if (!lockAcquired) {
                    return false;
                }
                long numberOfAcceptedRequests = requestInstants.stream().filter(instant1 -> instant1.isAfter(beginningOfSlice)).count();
                if (!(numberOfAcceptedRequests < maxLimit))
                    return false;
                requestInstants.add(instant);

                Iterator<Instant> it = requestInstants.iterator();
                List<Instant> toBeRemoved = new ArrayList<>();
                while (it.hasNext()) {
                    Instant current = it.next();
                    if (current.isBefore(beginningOfSlice)) {
//                        it.remove();
                        toBeRemoved.add(current);
                    }
                }
                requestInstants.removeAll(toBeRemoved);
                return true;
            } finally {
                if (lockAcquired) {
                    lock.writeLock().unlock();
                }
            }
        }

        private boolean tryAcquireLock(int retry) {
            Random random = new Random();
            int multiply = random.nextInt(10);
            boolean result = false;
            for (int i = 0; i < retry; i++) {
                try {
                    result = lock.writeLock().tryLock(100 * multiply, TimeUnit.SECONDS);
                    if (result)
                        break;
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            return result;
        }

    }

    static final class Key {
        private final String userAgent;
        private final String ipAddress;

        public Key(String userAgent, String ipAddress) {
            this.userAgent = userAgent;
            this.ipAddress = ipAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (!Objects.equals(userAgent, key.userAgent)) return false;
            return Objects.equals(ipAddress, key.ipAddress);
        }

        @Override
        public int hashCode() {
            int result = userAgent != null ? userAgent.hashCode() : 0;
            result = 31 * result + (ipAddress != null ? ipAddress.hashCode() : 0);
            return result;
        }
    }

}

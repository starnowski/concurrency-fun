package com.github.starnowski.concurrency.fun.java21;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RateLimiterImpl implements RateLimiter {

    private final Clock clock;
    private final int maxLimit;
    private final Duration slicePeriod;
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
//        long numberOfAcceptedRequests = workUnit.requestInstants.stream().filter(instant1 -> instant1.isAfter(beginningOfSlice)).count();
//        if (numberOfAcceptedRequests < maxLimit) {
//            try {
//                workUnit.lock.readLock().tryLock(10, TimeUnit.SECONDS);
//                numberOfAcceptedRequests = workUnit.requestInstants.stream().filter(instant1 -> instant1.isAfter(beginningOfSlice)).count();
//                if (!(numberOfAcceptedRequests < maxLimit))
//                    return false;
//                try {
//                    workUnit.lock.writeLock().lock();
//                    workUnit.requestInstants.add(instant);
//
//                    Iterator<Instant> it = workUnit.requestInstants.iterator();
//                    while (it.hasNext()) {
//                        Instant current = it.next();
//                        if (current.isBefore(beginningOfSlice)) {
//                            it.remove();
//                        }
//                    }
//                } finally {
//                    workUnit.lock.writeLock().unlock();
//                }
//                return true;
//                //TODO unlock
//            } catch (InterruptedException e) {
//                return false;
//            } finally {
////                workUnit.lock.writeLock().v();
//                workUnit.lock.readLock().unlock();
//            }
//            workUnit.requestInstants.add(instant);
//
//            Iterator<Instant> it = workUnit.requestInstants.iterator();
//            while(it.hasNext())  {
//                Instant current = it.next();
//                if (current.isBefore(beginningOfSlice)) {
//                    it.remove();
//                }
//            }

//            map.merge(key, new WorkUnit(), (cur, next) -> {
//                cur.requestInstants.add(instant);
//
//                Iterator<Instant> it = cur.requestInstants.iterator();
//                while (it.hasNext()) {
//                    Instant current = it.next();
//                    if (current.isBefore(beginningOfSlice)) {
//                        it.remove();
//                    }
//                }
//                return cur;
//            });
//            return true;
//        }
        return workUnit.tryRegisterRequestWhenCanBeAccepted(instant, beginningOfSlice, maxLimit);
    }

    private Key prepareKey(String userAgent, String ipAddress) {
        return new Key(userAgent, ipAddress);
    }

    private static class WorkUnit {

        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private List<Instant> requestInstants = new ArrayList<>();

        public List<Instant> getRequestInstants() {
            return requestInstants;
        }

        public void setRequestInstants(List<Instant> requestInstants) {
            this.requestInstants = requestInstants;
        }

        public boolean tryRegisterRequestWhenCanBeAccepted(Instant instant, Instant beginningOfSlice, int maxLimit) {
            try {
//                lock.writeLock().tryLock(10, TimeUnit.SECONDS);
                lock.writeLock().lock();
                long numberOfAcceptedRequests = requestInstants.stream().filter(instant1 -> instant1.isAfter(beginningOfSlice)).count();
                if (!(numberOfAcceptedRequests < maxLimit))
                    return false;
                requestInstants.add(instant);

                Iterator<Instant> it = requestInstants.iterator();
                while (it.hasNext()) {
                    Instant current = it.next();
                    if (current.isBefore(beginningOfSlice)) {
                        it.remove();
                    }
                }
                return true;
            } finally {
                lock.writeLock().unlock();
            }
        }

    }

    private static final class Key {
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

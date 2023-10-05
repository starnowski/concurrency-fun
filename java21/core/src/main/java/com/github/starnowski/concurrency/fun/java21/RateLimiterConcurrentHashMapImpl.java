package com.github.starnowski.concurrency.fun.java21;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

public class RateLimiterConcurrentHashMapImpl implements RateLimiter {

    private final Clock clock;
    private final int maxLimit;
    private final Duration slicePeriod;
    private final ConcurrentHashMap<Key, WorkUnit> map = new ConcurrentHashMap<>();

    public RateLimiterConcurrentHashMapImpl(Clock clock) {
        this(clock, 5, Duration.ofSeconds(10 * 60));
    }

    public RateLimiterConcurrentHashMapImpl(Clock clock, int maxLimit, Duration slicePeriod) {
        this.clock = clock;
        this.maxLimit = maxLimit;
        this.slicePeriod = slicePeriod;
    }

    static Key prepareKey(String userAgent, String ipAddress) {
        return new Key(userAgent, ipAddress);
    }

    @Override
    public boolean canAccept(String userAgent, String ipAddress) {
        Key key = prepareKey(userAgent, ipAddress);
        Instant instant = this.clock.instant();
        Instant beginningOfSlice = instant.minus(this.slicePeriod);
        WorkUnit workUnit = new WorkUnit();
        RequestInstantWithUUID ri = new RequestInstantWithUUID(instant, UUID.randomUUID());
        workUnit.getRequestInstants().add(ri);
        WorkUnit currentUnit = map.merge(key, workUnit, (workUnit1, workUnit2) -> {
            long numberOfAcceptedRequestsX = workUnit1.getRequestInstants().stream().filter(requestInstantWithUUID -> requestInstantWithUUID.getInstant().isAfter(beginningOfSlice)).count();
            if (numberOfAcceptedRequestsX >= maxLimit) {
                return workUnit1;
            }
            WorkUnit newWorkUnit = new WorkUnit();
            newWorkUnit.getRequestInstants().addAll(workUnit1.getRequestInstants().stream().filter(requestInstantWithUUID -> requestInstantWithUUID.getInstant().isAfter(beginningOfSlice)).collect(toList()));
            newWorkUnit.getRequestInstants().addAll(workUnit2.getRequestInstants().stream().filter(requestInstantWithUUID -> requestInstantWithUUID.getInstant().isAfter(beginningOfSlice)).collect(toList()));
            return newWorkUnit;
        });
        return currentUnit.getRequestInstants().stream().anyMatch(unit -> ri.getUuid().equals(unit.getUuid()));
    }

    /**
     * For tests purpose
     *
     * @return
     */
    ConcurrentHashMap<Key, WorkUnit> getMap() {
        return map;
    }

    public void cleanOldWorkUnits() {
    }

    static class WorkUnit {
        //TODO do immutable object
        private final List<RequestInstantWithUUID> requestInstants = new ArrayList<>();

        List<RequestInstantWithUUID> getRequestInstants() {
            return requestInstants;
        }
    }

    static class RequestInstantWithUUID {
        private final Instant instant;
        private final UUID uuid;

        public RequestInstantWithUUID(Instant instant, UUID uuid) {
            this.instant = instant;
            this.uuid = uuid;
        }

        public Instant getInstant() {
            return instant;
        }

        public UUID getUuid() {
            return uuid;
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

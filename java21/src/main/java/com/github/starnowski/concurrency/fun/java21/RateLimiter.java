package com.github.starnowski.concurrency.fun.java21;

interface RateLimiter {
    boolean canAccept(String userAgent, String ipAddress);
}
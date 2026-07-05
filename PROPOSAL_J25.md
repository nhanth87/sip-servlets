# Sip-Servlets J25 Compact Edition — Architecture Proposal

## 🎯 Goal
Transform RestComm Sip-Servlets into a **compact, high-performance SIP application server** targeting:
- **Java 25** (Virtual Threads, Scoped Values, Structured Concurrency)
- **Quarkus** (native compilation, fast startup) 
- **Spring Boot 3.x** (latest, with virtual thread support)
- **LMAX Disruptor** (ultra-low-latency message dispatching)
- **Netty HashedWheelTimer** (from jSS7 — replacing old ScheduledThreadPoolExecutor)

---

## 📦 Phase 1: CLEANUP — Remove Legacy

### Modules to DELETE:
| Module | Reason |
|--------|--------|
| `sip-servlets-jruby` | JRuby support — irrelevant |
| `containers/tomcat-7` | EOL Tomcat 7 |
| `containers/tomcat-8` | EOL Tomcat 8 |
| `containers/sip-servlets-catalina-7` | Catalina 7 — EOL |
| `containers/sip-servlets-catalina-8` | Catalina 8 — EOL |
| `containers/sip-servlets-as7` | JBoss AS7 — EOL |
| `containers/sip-servlets-as7-drop-in` | AS7 drop-in — EOL |
| `containers/sip-servlets-as8` | WildFly 8 — EOL |
| `containers/sip-servlets-as8-drop-in` | WF8 drop-in — EOL |
| `sip-servlets-arquillian-testsuite` | Arquillian tests — heavy, legacy |
| `sip-servlets-test-suite/sipp-scenarios` | SIPp scenarios — heavy |
| `sip-servlets-test-suite/tck` | TCK — heavy |
| `build/jsr289-tck` | TCK build |
| `build/jsr289-apidocs` | Old javadoc |
| `management/sip-servlets-management` | JMX management |
| `sip-servlets-examples/*` (most) | Keep only 2-3 key examples |

### Modules to KEEP (core engine):
| Module | Role |
|--------|------|
| `sip-servlets-spec` | JSR 289 SIP Servlet API (`javax.servlet.sip.*`) |
| `sip-servlets-core-api` | Mobicents SIP core interfaces |
| `sip-servlets-impl` | **Core SIP engine** (proxy, B2BUA, sessions) |
| `sip-servlets-client` | SIP connector client |
| `sip-servlets-application-router` | Application Router (DAR) |
| `sip-servlets-annotations` | Concurrency annotations |

### Modules to ADD:
| Module | Role |
|--------|------|
| `sip-servlets-quarkus` | Quarkus extension |

---

## ⚡ Phase 2: TIMER REPLACEMENT

### Current (old): ScheduledThreadPoolExecutor
```java
// TimerServiceImpl.java — blocks on queue, poor scaling
scheduledExecutor = new ScheduledThreadPoolExecutor(4, ...);
```

### Target (new): Netty HashedWheelTimer from jSS7
**Copy source** directly (no Maven dep):
```
org/mobicents/sip/timer/
├── HashedWheelTimer.java       // O(1) insert
├── Timeout.java                // timeout handle
├── TimerTask.java              // base task
└── SipServletTimerBridge.java  // bridge to TimerService
```
```java
timer = new HashedWheelTimer(Thread.ofVirtual().factory(), 100, MILLISECONDS, 512);
timer.newTimeout(t -> Thread.startVirtualThread(() -> listener.timeout(e)), delay, MILLISECONDS);
```



---

## 🚀 Phase 3: LMAX DISRUPTOR + VIRTUAL THREADS

### Current: ExecutorService (head-of-line blocking)
```
JainSip → SipApplicationDispatcherImpl → DispatchTask(Runnable) → ExecutorService
```

### Target: Disruptor RingBuffer + Virtual Threads
```
JainSip Listener (NIO/VT)
       │
       ▼
┌────────────────────────────┐
│  LMAX Disruptor RingBuffer │  ← Single-producer, multi-consumer
│  (32768 slots, BusySpin)   │
└────────────────────────────┘
       │        │        │
       ▼        ▼        ▼
  ┌────────┐ ┌────────┐ ┌────────┐
  │  INIT  │ │  SUB   │ │  RESP  │  ← 3 parallel EventHandlers
  │Request │ │  Req   │ │  onse  │
  └────────┘ └────────┘ └────────┘
       │        │        │
       ▼        ▼        ▼
  ┌────────────────────────────┐
  │  Virtual Thread per Msg    │  ← Java 25 VT: scalable to millions
  │  Thread.startVirtualThread │
  └────────────────────────────┘
```

```java
Disruptor<SipMessageEvent> disruptor = new Disruptor<>(
    SipMessageEvent::new, 32768,
    Thread.ofVirtual().factory(),  // VT factory
    ProducerType.SINGLE, new BusySpinWaitStrategy()
);

disruptor.handleEventsWith(
    new InitialRequestHandler(), new SubsequentRequestHandler()
).then(new ResponseHandler());
```



---

## 🔧 Phase 4: QUARKUS + SPRING BOOT

### Quarkus Extension
```
sip-servlets-quarkus/
├── SipServletsExtension.java      // Quarkus extension
├── SipServletsConfig.java         // @ConfigProperties
├── SipContainer.java              // CDI singleton
└── META-INF/quarkus-extension.yaml
```

### Spring Boot Starter
```
sip-servlets-spring-boot/
├── SipServletsAutoConfiguration.java
├── SipServletsProperties.java     // @ConfigurationProperties
└── META-INF/spring.factories
```

### Dependencies to add:
```xml
<dependency>
    <groupId>com.lmax</groupId>
    <artifactId>disruptor</artifactId>
    <version>3.4.4</version>
</dependency>
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.100.Final</version>
</dependency>
```

---

## 📅 Timeline
| Phase | Days | Description |
|-------|------|-------------|
| Phase 1 | 2 | Cleanup + compact |
| Phase 2 | 2 | Timer (jSS7 HashedWheelTimer) |
| Phase 3 | 5 | LMAX + Virtual Threads |
| Phase 4 | 3 | Quarkus + Spring Boot |
| **Total** | **12** | |

## ✅ Ready? Let's GO!

### Performance targets:
- **TPS**: 50,000+ SIP msg/sec (vs ~5,000 current)
- **P99**: < 1ms for proxy
- **Heap**: < 512MB for 10K concurrent calls

| `sip-servlets-spring-boot` | Spring Boot starter |
| `sip-servlets-netty` | Netty transport (UDP/TCP SIP)
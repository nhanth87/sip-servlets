package org.mobicents.servlet.sip.core;

import gov.nist.javax.sip.*;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.ar.*;
import javax.sip.*;
import javax.sip.header.*;
import javax.sip.message.*;
import org.apache.log4j.Logger;
import org.mobicents.ext.javax.sip.dns.DNSServerLocator;
import org.mobicents.javax.servlet.CongestionControlPolicy;
import org.mobicents.javax.servlet.sip.dns.DNSResolver;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;

public class SipApplicationDispatcherImpl implements SipApplicationDispatcher, SipListenerExt {
    private static final Logger logger = Logger.getLogger(SipApplicationDispatcherImpl.class);
    public static final String INTERVAL_ATT = "interval";
    public long congestionControlCheckingInterval = 30000;
    public Map<String, SipContext> applicationDeployed = new ConcurrentHashMap<>();
    protected SipApplicationRouter sipApplicationRouter;
    private SipStack sipStack;
    private DNSResolver dnsResolver;
    private DNSServerLocator dnsServerLocator;
    private SipService sipService;
    protected DispatcherFSM fsm = new DispatcherFSM(this);
    
    // Inner classes needed by DispatcherFSM
    class AddAppAction implements DispatcherFSM.Action {
        public void execute(DispatcherFSM.Context ctx) {}
    }
    class RemoveApp implements DispatcherFSM.Action {
        public void execute(DispatcherFSM.Context ctx) { applicationDeployed.remove((String)ctx.lastEvent.data.get("AppName")); }
    }
    class SetCongAction implements DispatcherFSM.Action {
        public void execute(DispatcherFSM.Context ctx) {}
    }
    class DisableCongAction implements DispatcherFSM.Action {
        public void execute(DispatcherFSM.Context ctx) {}
    }
    class ResetStatsAction implements DispatcherFSM.Action {
        public void execute(DispatcherFSM.Context ctx) {}
    }
    
    @Override public void init() { fsm.fireEvent(fsm.new Event(DispatcherFSM.EventType.START)); }
    @Override public void putInService() {}
    @Override public void start() { fsm.fireEvent(fsm.new Event(DispatcherFSM.EventType.START)); }
    @Override public void stop() { fsm.fireEvent(fsm.new Event(DispatcherFSM.EventType.STOP)); }
    @Override public void stopGracefully(long t) { stop(); }
    @Override public SipContext removeSipApplication(String name) { return applicationDeployed.remove(name); }
    @Override public void addSipApplication(String name, SipContext ctx) {
        applicationDeployed.put(name, ctx);
        DispatcherFSM.Event ev = fsm.new Event(DispatcherFSM.EventType.ADD_APP);
        ev.data.put("Context", ctx);
        fsm.fireEvent(ev);
    }
    @Override public List<SipURI> getOutboundInterfaces() { return Collections.emptyList(); }
    @Override public void addHostName(String h) {}
    @Override public void removeHostName(String h) {}
    @Override public Set<String> findHostNames() { return Collections.emptySet(); }
    @Override public String getDomain() { return "localhost"; }
    @Override public void setDomain(String d) {}
    @Override public boolean isRouteExternal(RouteHeader r) { return false; }
    @Override public boolean isViaHeaderExternal(ViaHeader v) { return false; }
    @Override public boolean isExternal(String h, int p, String t) { return false; }
    @Override public void sendSwitchoverInstruction(String a, String b) {}
    @Override public void setGracefulShutdown(boolean s) {}
    @Override public String getApplicationNameFromHash(String h) { return null; }
    @Override public String getHashFromApplicationName(String a) { return null; }
    @Override public ConcurrencyControlMode getConcurrencyControlMode() { return ConcurrencyControlMode.None; }
    @Override public void setConcurrencyControlMode(ConcurrencyControlMode m) {}
    @Override public String getConcurrencyControlModeByName() { return "None"; }
    @Override public void setConcurrencyControlModeByName(String n) {}
    @Override public int getQueueSize() { return 0; }
    @Override public void setQueueSize(int s) {}
    @Override public void setMemoryThreshold(int t) {}
    @Override public int getMemoryThreshold() { return 90; }
    @Override public void setCongestionControlCheckingInterval(long i) {}
    @Override public long getCongestionControlCheckingInterval() { return 30000; }
    @Override public CongestionControlPolicy getCongestionControlPolicy() { return CongestionControlPolicy.None; }
    @Override public void setCongestionControlPolicy(CongestionControlPolicy p) {}
    @Override public String getCongestionControlPolicyByName() { return "None"; }
    @Override public void setCongestionControlPolicyByName(String n) {}
    @Override public int getNumberOfMessagesInQueue() { return 0; }
    @Override public void setBypassRequestExecutor(boolean b) {}
    @Override public boolean isBypassRequestExecutor() { return true; }
    @Override public void setBypassResponseExecutor(boolean b) {}
    @Override public boolean isBypassResponseExecutor() { return true; }
    @Override public void setBaseTimerInterval(int i) {}
    @Override public int getBaseTimerInterval() { return 500; }
    @Override public void setT2Interval(int i) {}
    @Override public int getT2Interval() { return 4000; }
    @Override public void setT4Interval(int i) {}
    @Override public int getT4Interval() { return 5000; }
    @Override public void setTimerDInterval(int i) {}
    @Override public int getTimerDInterval() { return 32000; }
    @Override public Map<String, AtomicLong> getRequestsProcessedByMethod() { return Collections.emptyMap(); }
    @Override public Map<String, AtomicLong> getResponsesProcessedByStatusCode() { return Collections.emptyMap(); }
    @Override public long getRequestsProcessedByMethod(String m) { return 0; }
    @Override public long getResponsesProcessedByStatusCode(String s) { return 0; }
    @Override public Map<String, AtomicLong> getRequestsSentByMethod() { return Collections.emptyMap(); }
    @Override public Map<String, AtomicLong> getResponsesSentByStatusCode() { return Collections.emptyMap(); }
    @Override public long getRequestsSentByMethod(String m) { return 0; }
    @Override public long getResponsesSentByStatusCode(String s) { return 0; }
    @Override public void resetStatsCounters() {}
    @Override public void setGatherStatistics(boolean g) {}
    @Override public boolean isGatherStatistics() { return false; }
    @Override public void updateResponseStatistics(Response r, boolean p) {}
    @Override public void updateRequestsStatistics(Request r, boolean p) {}
    @Override public void incCalls() {}
    @Override public void incMessages() {}
    @Override public void incSeconds(long s) {}
    @Override public void setBackToNormalMemoryThreshold(int t) {}
    @Override public int getBackToNormalMemoryThreshold() { return 80; }
    @Override public void setBackToNormalQueueSize(int s) {}
    @Override public int getBackToNormalQueueSize() { return 0; }
    @Override public void setSipStack(SipStack s) { this.sipStack = s; }
    @Override public void setDNSServerLocator(DNSServerLocator d) { this.dnsServerLocator = d; }
    @Override public DNSServerLocator getDNSServerLocator() { return dnsServerLocator; }
    @Override public void setDNSTimeout(int t) {}
    @Override public int getDNSTimeout() { return 5; }
    @Override public DNSResolver getDNSResolver() { return dnsResolver; }
    @Override public void setDNSResolver(DNSResolver r) { this.dnsResolver = r; }
    @Override public String getVersion() { return "3.0.0-j25"; }
    @Override public Map<String, List<? extends SipApplicationRouterInfo>> getApplicationRouterConfiguration() { return Collections.emptyMap(); }
    @Override public Object retrieveApplicationRouterConfiguration() { return null; }
    @Override public void updateApplicationRouterConfiguration(Object o) {}
    @Override public Serializable retrieveApplicationRouterConfigurationString() { return null; }
    @Override public void updateApplicationRouterConfiguration(Serializable s) {}
    @Override public String[] findInstalledSipApplications() { return new String[0]; }
    @Override public SipService getSipService() { return sipService; }
    @Override public void setSipService(SipService s) { this.sipService = s; }
    @Override public String getApplicationServerId() { return "j25"; }
    @Override public String getApplicationServerIdHash() { return "j25"; }
    @Override public int getTagHashMaxLength() { return 24; }
    @Override public ExecutorService getAsynchronousExecutor() { return Executors.newFixedThreadPool(4); }
    @Override public void setAsynchronousExecutor(ExecutorService e) {}
        @Override public String getCallId(MobicentsExtendedListeningPoint lp, String host) { return "j25-call-id"; }
    @Override public void processRequest(RequestEvent e) {}
    @Override public void processResponse(ResponseEvent e) {}
    @Override public void processTimeout(TimeoutEvent e) {}
    @Override public void processIOException(IOExceptionEvent e) {}
    @Override public void processTransactionTerminated(TransactionTerminatedEvent e) {}
    @Override public void processDialogTerminated(DialogTerminatedEvent e) {}
}

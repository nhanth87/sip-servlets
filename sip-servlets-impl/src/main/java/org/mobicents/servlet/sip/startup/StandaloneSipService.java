package org.mobicents.servlet.sip.startup;

import javax.sip.SipStack;

import org.apache.log4j.Logger;
import org.mobicents.ha.javax.sip.ReplicationStrategy;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipService;
import org.mobicents.servlet.sip.core.message.OutboundProxy;

/**
 * Standalone SipService implementation — container-independent bootstrap for Java 25 compact edition.
 * Replaces Tomcat/Undertow/JBoss-specific SipStandardService implementations.
 */
public class StandaloneSipService implements SipService {

    private static final Logger logger = Logger.getLogger(StandaloneSipService.class);

    private SipApplicationDispatcher sipApplicationDispatcher;
    private SipStack sipStack;
    private SipConnector[] sipConnectors;
    private OutboundProxy outboundProxy;
    private String mobicentsSipServletMessageFactoryClassName;
    private String proxyTimerServiceType = "Default";
    private String sasTimerServiceType = "Default";
    private int dispatcherThreadPoolSize = 64;
    private int canceledTimerTasksPurgePeriod = 30;
    private String jvmRoute = "";
    private boolean httpFollowsSip = false;
    private boolean dialogPendingRequestChecking = false;
    private boolean md5ContactUserPart = false;
    private int tagHashMaxLength = 24;
    private int callIdMaxLength = 128;
    private String dnsResolverClass = "";

    public void initialize(SipApplicationDispatcher dispatcher, SipStack stack, SipConnector[] connectors) {
        this.sipApplicationDispatcher = dispatcher;
        this.sipStack = stack;
        this.sipConnectors = connectors;
        StaticServiceHolder.sipStandardService = this;
        logger.info("StandaloneSipService initialized");
    }

    @Override
    public SipApplicationDispatcher getSipApplicationDispatcher() {
        return sipApplicationDispatcher;
    }

    @Override
    public void setSipApplicationDispatcher(SipApplicationDispatcher sipApplicationDispatcher) {
        this.sipApplicationDispatcher = sipApplicationDispatcher;
    }

    @Override
    public SipStack getSipStack() {
        return sipStack;
    }

    @Override
    public SipConnector findSipConnector(String transport) {
        if (sipConnectors != null) {
            for (SipConnector c : sipConnectors) {
                if (c.getTransport().equalsIgnoreCase(transport)) {
                    return c;
                }
            }
        }
        // fallback: create default connector
        SipConnector defaultConnector = new SipConnector();
        defaultConnector.setTransport(transport);
        defaultConnector.setPort(5060);
        defaultConnector.setIpAddress("0.0.0.0");
        return defaultConnector;
    }

    @Override
    public SipConnector[] findSipConnectors() {
        return sipConnectors != null ? sipConnectors : new SipConnector[0];
    }

    @Override
    public boolean isHttpFollowsSip() { return httpFollowsSip; }

    @Override
    public String getJvmRoute() { return jvmRoute; }

    @Override
    public OutboundProxy getOutboundProxy() { return outboundProxy; }

    public void setOutboundProxy(OutboundProxy outboundProxy) { this.outboundProxy = outboundProxy; }

    @Override
    public int getDispatcherThreadPoolSize() { return dispatcherThreadPoolSize; }

    public void setDispatcherThreadPoolSize(int size) { this.dispatcherThreadPoolSize = size; }

    @Override
    public int getCanceledTimerTasksPurgePeriod() { return canceledTimerTasksPurgePeriod; }

    @Override
    public boolean isDialogPendingRequestChecking() { return dialogPendingRequestChecking; }

    @Override
    public boolean isMd5ContactUserPart() { return md5ContactUserPart; }

    @Override
    public ReplicationStrategy getReplicationStrategy() { return ReplicationStrategy.None; }

    @Override
    public int getTagHashMaxLength() { return tagHashMaxLength; }

    @Override
    public int getCallIdMaxLength() { return callIdMaxLength; }

    @Override
    public String getDnsResolverClass() { return dnsResolverClass; }

    @Override
    public int getDnsTimeout() { return 5; }

    @Override
    public String getMobicentsSipServletMessageFactoryClassName() {
        return mobicentsSipServletMessageFactoryClassName;
    }

    @Override
    public void setMobicentsSipServletMessageFactoryClassName(String name) {
        this.mobicentsSipServletMessageFactoryClassName = name;
    }

    @Override
    public void stopGracefully(long timeToWait) {
        logger.info("StandaloneSipService stopping gracefully...");
        if (sipStack != null) {
            sipStack.stop();
        }
    }

    @Override
    public String getProxyTimerServiceImplementationType() { return proxyTimerServiceType; }

    @Override
    public String getSasTimerServiceImplementationType() { return sasTimerServiceType; }
}

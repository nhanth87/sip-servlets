package org.mobicents.ext.javax.sip.dns;

import javax.sip.address.Hop;
import javax.sip.address.Router;

/** Stub - DNS-aware router for J25 compact edition */
public interface DNSAwareRouter extends Router {
    Hop getNextHop(String transport);
    void reset();
}

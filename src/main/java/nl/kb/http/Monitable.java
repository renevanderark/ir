package nl.kb.http;

import java.util.List;

public interface Monitable {

    List<ConnectionMonit> getOpenConnections();

    void disconnectStalledConnections(int maximumDownloadStallTimeMs);
}

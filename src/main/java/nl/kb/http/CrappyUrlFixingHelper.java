package nl.kb.http;

import java.net.URL;
import java.util.Arrays;

class CrappyUrlFixingHelper {

    private CrappyUrlFixingHelper() {

    }

    static String fixCrappyLocationHeaderValue(URL originalUrl, String redirectLocation) {

        final boolean usePercents = originalUrl.toString().contains("%20");
        final String locationWithProtocolAndHost = redirectLocation.startsWith("/")
                ? originalUrl.getProtocol() + "://" + originalUrl.getHost() + redirectLocation
                : redirectLocation;

        return fix(locationWithProtocolAndHost, usePercents);
    }

    private static String fix(String locationWithProtocolAndHost, boolean usePercents) {
        final int lastIndexOfSlash = locationWithProtocolAndHost.lastIndexOf('/');

        final String pathBit = locationWithProtocolAndHost.substring(0, lastIndexOfSlash + 1);
        final String nameAndQueryBit = locationWithProtocolAndHost.substring(lastIndexOfSlash + 1);

        final String nameBit = Arrays.asList(nameAndQueryBit.split("[\\?;]")).get(0);
        final String encodedNameBit = usePercents
                ? nameBit.replaceAll(" ", "%20")
                : nameBit.replaceAll(" ", "+");

        return pathBit + encodedNameBit + nameAndQueryBit.substring(nameBit.length());
    }


}

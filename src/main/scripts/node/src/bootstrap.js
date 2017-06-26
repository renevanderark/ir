import {connectSocket} from "./actions/socket-listener";
import {fetchInitialData} from "./actions/fetch-initial-data";

// Checks for exsistence of authentication token
const checkForAuthToken = (token, urlToken) => {
    if (urlToken !== null) {
        // If part of the URL redirect to root
        localStorage.setItem("authToken", urlToken);
        location.href = "/";
        return false;
    } else if (token === null) {
        // If not present at all, redirect to kbaut
        location.href = `${globals.kbAutLocation}?id=dare2&application=ir-objectharvester&return_url=` +
            encodeURIComponent(`http://${globals.hostName}/authenticate`);
        return false;
    }
    return true;
};

// Renders the app when token is present
const authenticateAndInitialize = (onInitialize) => {
    const tokenFromUrl = () => {
        return location.href.indexOf("token=") < 0 ?
            null :
            location.href.replace(/^.+token=/, "");
    };

    if (checkForAuthToken(localStorage.getItem("authToken"), tokenFromUrl())) {
        // Fetches initial render data via xhr and will invoke onInitialize callback when received
        fetchInitialData(onInitialize);
        // Connects to websocket, which receives updates on render data
        connectSocket();
    }
};

export { authenticateAndInitialize };
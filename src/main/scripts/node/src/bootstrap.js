import store from "./store";
import {fetchRepositories} from "./actions/repositories";
import {fetchErrorStatus, fetchRecordStatus} from "./actions/record-status";

import ActionTypes from "./action-types";

const connectSocket = () => {
    const webSocket = new WebSocket("ws://" + globals.hostName + "/status-socket");

    webSocket.onmessage = ({ data: msg}) => {
        const {type, data} = JSON.parse(msg);
        switch (type) {
            case "repository-change":
                store.dispatch({type: ActionTypes.RECEIVE_REPOSITORIES, data: data});
                break;
            case "record-change":
                store.dispatch({type: ActionTypes.RECEIVE_RECORD_STATUS, data: data});
                break;
            case "error-change":
                store.dispatch({type: ActionTypes.RECEIVE_ERROR_STATUS, data: data});
                break;
            case "record-fetcher":
                store.dispatch({type: ActionTypes.ON_FETCHER_RUNSTATE_CHANGE, data: data});
        }
    };

    // Keep the websocket alive
    const pingWs = () => {
        store.dispatch({type: ActionTypes.ON_STATUS_UPDATE});

        webSocket.send("* ping! *");
        window.setTimeout(pingWs, 8000);
    };

    webSocket.onclose = () => {
        store.dispatch({type: ActionTypes.ON_SOCKET_CLOSED});
        window.setTimeout(connectSocket, 500);
    };

    webSocket.onopen = pingWs;
};

const fetchInitialData = (onInitialize) => store.dispatch(fetchRepositories(() =>
    store.dispatch(fetchRecordStatus(() =>
        store.dispatch(fetchErrorStatus(onInitialize))))));


export { connectSocket, fetchInitialData };
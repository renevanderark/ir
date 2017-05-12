import ActionTypes from "../action-types";

const initialState = {
    socketClosed: true
};

export default function(state=initialState, action) {
    switch (action.type) {
        case ActionTypes.ON_STATUS_UPDATE:
            return {
                ...state,
                socketClosed: false
            };
        case ActionTypes.ON_SOCKET_CLOSED:
            return {
                ...state,
                socketClosed: true
            };
        default:
    }

    return state;
}
import ActionTypes from "../action-types";

const initialState = {
    socketClosed: true,
    statusCodes: null
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
        case ActionTypes.RECEIVE_STATUS_CODES:
            return {
                ...state,
                statusCodes: action.data
            };
        default:
    }

    return state;
}
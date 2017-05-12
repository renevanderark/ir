import ActionTypes from "../action-types";

const initialState = {
    list: [],
    pending: false
};


export default function(state=initialState, action) {
    switch (action.type) {
        case ActionTypes.REQUEST_REPOSITORIES:
            return {
                ...state,
                pending: true
            };
        case ActionTypes.RECEIVE_REPOSITORIES:
            return {
                ...state,
                list: (action.data || []),
                pending: false
            };
        default:
    }

    return state;
}
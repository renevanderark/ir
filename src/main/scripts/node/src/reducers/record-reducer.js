import ActionTypes from "../action-types";

const initialState = {
    list: []
};

export default function(state=initialState, action) {
    switch (action.type) {
        case ActionTypes.RECEIVE_FIND_RESULT:
            return {...state, list: action.data};
        case ActionTypes.CLEAR_FOUND_RECORDS:
            return {...state, list: []};
        default:
    }
    return state;
}
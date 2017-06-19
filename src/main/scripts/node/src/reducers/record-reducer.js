import ActionTypes from "../action-types";

const initialState = {
    list: [],
    record: null
};

export default function(state=initialState, action) {
    switch (action.type) {
        case ActionTypes.RECEIVE_FIND_RESULT:
            return {...state, list: action.data};
        case ActionTypes.CLEAR_FOUND_RECORDS:
            return {...state, list: []};
        case ActionTypes.RECEIVE_RECORD:
            return {...state, record: action.data};
        default:
    }
    return state;
}
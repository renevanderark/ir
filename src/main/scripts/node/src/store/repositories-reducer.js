import ActionTypes from "../actions/action-types";

const initialState = {
    list: [],
    pending: false,
    underEdit: null,
    validationResultsUnderEdit: {},
    recordStatus: {},
    errorStatus: {},
    harvesterRunStates: {}
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
        case ActionTypes.RECEIVE_NEW_REPOSITORY_VALIDATION_RESULTS:
            return {
                ...state,
                underEdit: action.underEdit,
                validationResultsUnderEdit: action.data
            };
        case ActionTypes.ON_SAVE_REPOSITORY:
            return {
                ...state,
                underEdit: null,
                validationResultsUnderEdit: {}
            };
        case ActionTypes.RECEIVE_RECORD_STATUS:
            return {
                ...state,
                recordStatus: action.data
            };
        case ActionTypes.RECEIVE_ERROR_STATUS:
            return {
                ...state,
                errorStatus: action.data
            };
        case ActionTypes.RECEIVE_HARVESTER_RUNSTATE:
            return {
                ...state,
                harvesterRunStates: action.data
            };
        default:
    }

    return state;
}
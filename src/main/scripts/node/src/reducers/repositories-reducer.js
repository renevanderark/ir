import ActionTypes from "../action-types";

const initialState = {
    list: [],
    pending: false,
    underEdit: null,
    validationResultsUnderEdit: {}
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
        default:
    }

    return state;
}
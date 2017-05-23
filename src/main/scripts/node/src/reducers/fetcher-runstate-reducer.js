import ActionTypes from "../action-types";
import { FetcherRunState } from "../enums";

const initialState = FetcherRunState.DISABLED;

export default function(state=initialState, action) {
    switch (action.type) {
        case ActionTypes.ON_FETCHER_RUNSTATE_CHANGE:
            return action.data;
        default:
    }

    return state;
}
import {combineReducers} from "redux";
import repositoriesReducer from "./repositories-reducer";
import statusReducer from "./status-reducer";
import fetcherRunstateReducer from "./fetcher-runstate-reducer";

export default combineReducers({
    status: statusReducer,
    repositories: repositoriesReducer,
    fetcherRunstate: fetcherRunstateReducer
});

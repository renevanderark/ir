import {combineReducers} from "redux";
import repositoriesReducer from "./repositories-reducer";
import statusReducer from "./status-reducer";

export default combineReducers({
    status: statusReducer,
    repositories: repositoriesReducer
});

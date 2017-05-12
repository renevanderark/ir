import {combineReducers} from "redux";
import repositoriesReducer from "./repositories-reducer";
export default combineReducers({
    repositories: repositoriesReducer
});

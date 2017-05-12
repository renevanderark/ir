import store from "./store";
import {fetchRepositories} from "./actions/repositories";

export default (onInitialize) => store.dispatch(fetchRepositories(onInitialize));
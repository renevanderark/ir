import React from "react";
import {Router, Route, IndexRoute, browserHistory} from "react-router";
import {Provider, connect} from "react-redux";

import store from "./store";
import actions from "./actions";

import rootConnector from "./connectors/root-connector";
import repositoriesConnector from "./connectors/repositories-connector";
import newRepositoryConnector from "./connectors/new-repository-connector";

import App from "./components/app";

import Repositories from "./components/repositories/repositories";
import NewRepository from "./components/repositories/new";

const urls = {
    root() {
        return "/";
    },
    newRepository() {
        return "/nieuw"
    }
};

export { urls };

const navigateTo = (key, args) => browserHistory.push(urls[key].apply(null, args));

const connectComponent = (stateToProps) => connect(stateToProps, dispatch => actions(navigateTo, dispatch));

export default (
    <Provider store={store}>
        <Router history={browserHistory}>
            <Route path={urls.root()} component={connectComponent(rootConnector)(App)}>
                <IndexRoute component={connectComponent(repositoriesConnector)(Repositories) } />
                <Route path={urls.newRepository()} component={connectComponent(newRepositoryConnector)(NewRepository)} />

            </Route>
        </Router>
    </Provider>
);

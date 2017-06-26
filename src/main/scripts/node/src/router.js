import React from "react";
import {Router, Route, IndexRoute, browserHistory} from "react-router";
import {Provider, connect} from "react-redux";

// The url definitions
import {urls} from "./etc/urls";

// The redux store
import store from "./store/store";

// The action definitions
import actions from "./actions/actions";

// The data transformers (from data in store to props in rendered component)
import rootConnector from "./connectors/root-connector";
import repositoriesConnector from "./connectors/repositories-connector";
import editRepositoryConnector from "./connectors/edit-repository-connector";
import repositoryStatusConnector from "./connectors/repository-status-connector";
import recordStatusConnector from "./connectors/record-status-connector";


// The root layout component
import App from "./components/app";

// The root components to be rendered via the router
import Repositories from "./components/repositories/repositories";
import NewRepository from "./components/repositories/new";
import EditRepository from "./components/repositories/edit";
import RepositoryStatus from "./components/repositories/repository-status";
import RecordStatus from "./components/record-status/record-status";


// Forces a navigation to the url identified by key (see ./etc/urls)
const navigateTo = (key, args) => browserHistory.push(urls[key].apply(null, args));

// Connects store data to component props
// see: https://github.com/reactjs/react-redux/blob/master/docs/api.md#connectmapstatetoprops-mapdispatchtoprops-mergeprops-options
const connectComponent = (stateToProps) => connect(stateToProps, dispatch => actions(navigateTo, dispatch));

// The routes:
// see: http://redux.js.org/docs/advanced/UsageWithReactRouter.html
export default (
    <Provider store={store}>
        <Router history={browserHistory}>
            <Route path={urls.root()} component={connectComponent(rootConnector)(App)}>
                <IndexRoute component={connectComponent(repositoriesConnector)(Repositories) } />
                <Route path={urls.newRepository()} component={connectComponent(editRepositoryConnector)(NewRepository)} />
                <Route path={urls.editRepository()} components={connectComponent(editRepositoryConnector)(EditRepository)} />
                <Route path={urls.repositoryStatus()} components={connectComponent(repositoryStatusConnector)(RepositoryStatus)}/>
                <Route path={urls.record()} components={connectComponent(recordStatusConnector)(RecordStatus)} />
            </Route>
        </Router>
    </Provider>
);

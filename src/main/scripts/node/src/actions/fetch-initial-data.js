import store from "../store/store";
import {fetchRepositories} from "./repositories";
import {fetchErrorStatus, fetchRecordStatus, fetchStatusCodes } from "./record-status";
import {fetchCredentials} from "./credentials"
import {fetchHarvesterStatus} from "./harvesters";

// Perform xhr requests for initial page render
const fetchInitialData = (onInitialize) => {
    // Fetch the list of repositories
    store.dispatch(fetchRepositories(() =>
        // Then fetch the current run state of any harvest definition
        store.dispatch(fetchHarvesterStatus(() =>
            // Then fetch the status code definitions
            store.dispatch(fetchStatusCodes(() =>
                // Then fetch the record status counts per repository id
                store.dispatch(fetchRecordStatus(() =>
                    // Then fetch the error status counts per repository id
                    store.dispatch(fetchErrorStatus(onInitialize))
                ))
            ))
        ))
    ));
    store.dispatch(fetchCredentials());
};


export { fetchInitialData }
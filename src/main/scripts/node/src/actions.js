import xhr from "xhr";

import {
    enableRepository,
    disableRepository,
    validateNewRepository,
    saveRepository,
    deleteRepository
} from "./actions/repositories";

import {
    startHarvest,
    interruptHarvest
} from "./actions/harvesters";

import {fetchInitialData} from "./bootstrap";
import {handleResponse} from "./actions/response-handler";
import ActionTypes from "./action-types";

const startOaiRecordFetcher = () => (dispatch) =>
    xhr({url: "/workers/start", "method": "PUT", headers: { 'Authorization': localStorage.getItem("authToken") }},
        (err, resp, body) => handleResponse(resp));


const disableOaiRecordFetcher = () => (dispatch) =>
    xhr({url: "/workers/disable", "method": "PUT", headers: { 'Authorization': localStorage.getItem("authToken") }},
        (err, resp, body) => handleResponse(resp));


const findRecords = (query) => (dispatch) => {
    dispatch({type: ActionTypes.FIND_RESULT_PENDING});

    xhr({url: `/records/find?q=${encodeURIComponent(query)}`, "method": "GET", headers: {
        'Authorization': localStorage.getItem("authToken") }}, (err, resp, body) => handleResponse(resp, () => {
        dispatch({type: ActionTypes.RECEIVE_FIND_RESULT, data: JSON.parse(body)});
    }));
};

export default function actionsMaker(navigateTo, dispatch) {
    return {
        onEnableRepository: (id) => dispatch(enableRepository(id)),
        onDisableRepository: (id) => dispatch(disableRepository(id)),
        onValidateNewRepository: (repository) => dispatch(validateNewRepository(repository)),
        onStartHarvest: (repositoryId) => dispatch(startHarvest(repositoryId)),
        onInterruptHarvest: (repositoryId) => dispatch(interruptHarvest(repositoryId)),
        onSaveRepository: () => dispatch(saveRepository(() => navigateTo("root"))),
        onDeleteRepository: (id) => dispatch(deleteRepository(id, () => navigateTo("root", []))),
        onRefetchRepositories: () => fetchInitialData(() => {}),

        onStartOaiRecordFetcher: () => dispatch(startOaiRecordFetcher()),
        onDisableOaiRecordFetcher: () => dispatch(disableOaiRecordFetcher()),

        onFindRecords: (query) => dispatch(findRecords(query)),
        onClearFoundRecords: () => dispatch({type: ActionTypes.CLEAR_FOUND_RECORDS}),
        onNavigateTo: (key, params) => navigateTo(key, params)
    };
}
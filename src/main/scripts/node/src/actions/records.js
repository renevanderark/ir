import xhr from "xhr";
import {handleResponse} from "./response-handler";
import ActionTypes from "./action-types";

const findRecords = (query) => (dispatch) => {
    dispatch({type: ActionTypes.FIND_RESULT_PENDING});

    xhr({url: `/records/find?q=${encodeURIComponent(query)}`, "method": "GET", headers: {
        'Authorization': localStorage.getItem("authToken") }}, (err, resp, body) => handleResponse(resp, () => {
        dispatch({type: ActionTypes.RECEIVE_FIND_RESULT, data: JSON.parse(body)});
    }));
};

const fetchRecord = (kbObjId) => (dispatch) => {
    dispatch({type: ActionTypes.RECORD_PENDING});

    xhr({url: `/records/status/${kbObjId}`, "method": "GET", headers: {
        'Authorization': localStorage.getItem("authToken") }}, (err, resp, body) => handleResponse(resp, () => {
        dispatch({type: ActionTypes.RECEIVE_RECORD, data: JSON.parse(body)});
    }));
};

export {findRecords, fetchRecord}
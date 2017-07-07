import xhr from "xhr";
import {handleResponse} from "./response-handler";
import ActionTypes from "./action-types";

const startOaiRecordFetcher = () => (dispatch) =>
    xhr({url: "/workers/start", "method": "PUT", headers: { 'Authorization': localStorage.getItem("authToken") }},
        (err, resp, body) => handleResponse(resp));


const disableOaiRecordFetcher = () => (dispatch) =>
    xhr({url: "/workers/disable", "method": "PUT", headers: { 'Authorization': localStorage.getItem("authToken") }},
        (err, resp, body) => handleResponse(resp));

const fetchOaiRecordFetcherStatus = (next = () => {}) => (dispatch) =>
    xhr({url: "/workers/status", "method": "GET", headers: { 'Authorization': localStorage.getItem("authToken") }},
        (err, resp, body) => handleResponse(resp, () => {
            dispatch({type: ActionTypes.ON_FETCHER_RUNSTATE_CHANGE, data: JSON.parse(body).data})
            next();
        }

));


export {startOaiRecordFetcher, disableOaiRecordFetcher, fetchOaiRecordFetcherStatus}
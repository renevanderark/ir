import xhr from "xhr";
import {handleResponse} from "./response-handler";

const startOaiRecordFetcher = () => (dispatch) =>
    xhr({url: "/workers/start", "method": "PUT", headers: { 'Authorization': localStorage.getItem("authToken") }},
        (err, resp, body) => handleResponse(resp));


const disableOaiRecordFetcher = () => (dispatch) =>
    xhr({url: "/workers/disable", "method": "PUT", headers: { 'Authorization': localStorage.getItem("authToken") }},
        (err, resp, body) => handleResponse(resp));

export {startOaiRecordFetcher, disableOaiRecordFetcher}
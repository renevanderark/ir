import xhr from "xhr";
import ActionTypes from "./action-types";
import {handleResponse} from "./response-handler";

const fetchRecordStatus = (next = () => {}) => (dispatch) => {
    xhr({url: `/record-status?${new Date().getTime()}`, method: "GET", headers: {'Authorization': localStorage.getItem("authToken")}},
        (err, resp, body) => handleResponse(resp, () => {
            dispatch({type: ActionTypes.RECEIVE_RECORD_STATUS, data: JSON.parse(body)});
            next();
        })
    );
};

const fetchErrorStatus = (next = () => {}) => (dispatch) => {
    xhr({url: `/record-status/errors?${new Date().getTime()}`, method: "GET", headers: {'Authorization': localStorage.getItem("authToken")}},
        (err, resp, body) => handleResponse(resp, () => {
            dispatch({type: ActionTypes.RECEIVE_ERROR_STATUS, data: JSON.parse(body)});
            next();
        })
    );
};

const fetchStatusCodes = (next = () => {}) => (dispatch) => {
    xhr({url: `/record-status/status-codes`, method: "GET", headers: {'Authorization': localStorage.getItem("authToken")}},
        (err, resp, body) => handleResponse(resp, () => {
            dispatch({type: ActionTypes.RECEIVE_STATUS_CODES, data: JSON.parse(body)});
            next();
        })
    );
};




export {fetchRecordStatus, fetchErrorStatus, fetchStatusCodes};
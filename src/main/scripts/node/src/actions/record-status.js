import xhr from "xhr";
import ActionTypes from "../action-types";

const fetchRecordStatus = (next = () => {}) => (dispatch) => {
    xhr({url: `/record-status?${new Date().getTime()}`, method: "GET"}, (err, resp, body) => {
        dispatch({type: ActionTypes.RECEIVE_RECORD_STATUS, data: JSON.parse(body)});
        next();
    });
};

const fetchErrorStatus = (next = () => {}) => (dispatch) => {
    xhr({url: `/record-status/errors?${new Date().getTime()}`, method: "GET"}, (err, resp, body) => {
        dispatch({type: ActionTypes.RECEIVE_ERROR_STATUS, data: JSON.parse(body)});
        next();
    });
};

const fetchStatusCodes = (next = () => {}) => (dispatch) => {
    xhr({url: `/record-status/status-codes`, method: "GET"}, (err, resp, body) => {
        dispatch({type: ActionTypes.RECEIVE_STATUS_CODES, data: JSON.parse(body)});
        next();
    });
};



export {fetchRecordStatus, fetchErrorStatus, fetchStatusCodes};
import xhr from "xhr";
import ActionTypes from "../action-types";

const fetchRecordStatus = (next = () => {}) => (dispatch) => {
    xhr({url: `/record-status?${new Date().getTime()}`, method: "GET"}, (err, resp, body) => {
        dispatch({type: ActionTypes.RECEIVE_RECORD_STATUS, data: JSON.parse(body).data});
        next();
    });
};

export {fetchRecordStatus};
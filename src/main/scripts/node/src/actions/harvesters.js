import xhr from "xhr";
import {handleResponse} from "./response-handler";
import ActionTypes from "./action-types";

const startHarvest = (repositoryId) => (distpatch) =>
    xhr({
        method: "POST",
        headers: {
          'Accept': "application/json",
          'Authorization': localStorage.getItem("authToken")
        },
        uri: `/harvesters/${repositoryId}/start`
    }, (err, resp, body) => {
        handleResponse(resp);
    });

const interruptHarvest = (repositoryId) => (distpatch) =>
    xhr({
        method: "POST",
        headers: {
            'Accept': "application/json",
            'Authorization': localStorage.getItem("authToken")
        },
        uri: `/harvesters/${repositoryId}/interrupt`
    }, (err, resp, body) => {
        handleResponse(resp);
    });

const fetchHarvesterStatus = (next = () => {}) => (dispatch) => {
    xhr({url: `/harvesters/status?${new Date().getTime()}`, method: "GET", headers: {'Authorization': localStorage.getItem("authToken")}},
        (err, resp, body) => handleResponse(resp, () => {
            dispatch({type: ActionTypes.RECEIVE_HARVESTER_RUNSTATE, data: JSON.parse(body)});
            next();
        })
    );
};

export { startHarvest, interruptHarvest, fetchHarvesterStatus }
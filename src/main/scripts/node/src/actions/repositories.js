import xhr from "xhr";
import ActionTypes from "../action-types";

const fetchRepositories = (next = () => {}) => (dispatch) => {
    dispatch({type: ActionTypes.REQUEST_REPOSITORIES});

    xhr({url: `/repositories?${new Date().getTime()}`, method: "GET"}, (err, resp, body) => {
        dispatch({type: ActionTypes.RECEIVE_REPOSITORIES, data: JSON.parse(body)});
        next();
    });
};

const toggleRepositoryEnabled = (id, operation) => (dispatch) =>
    xhr({url: `/repositories/${id}/${operation}`, method: "PUT"}, () => {});

const enableRepository = (id) => toggleRepositoryEnabled(id, "enable");

const disableRepository = (id) => toggleRepositoryEnabled(id, "disable");

const setHarvestSchedule = (id, scheduleEnumValue) => (dispatch) =>
    xhr({
        url: `/repositories/${id}/setSchedule/${scheduleEnumValue}`,
        method: "PUT",
    }, () => {});


export {
    fetchRepositories,
    enableRepository,
    disableRepository,
    setHarvestSchedule
};
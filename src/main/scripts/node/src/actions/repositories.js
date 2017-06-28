import xhr from "xhr";
import ActionTypes from "./action-types";
import { Schedule } from "../etc/enums";
import {handleResponse} from "./response-handler";

const fetchRepositories = (next = () => {}) => (dispatch) => {
    dispatch({type: ActionTypes.REQUEST_REPOSITORIES});

    xhr({url: `/repositories?${new Date().getTime()}`, method: "GET", headers: {'Authorization': localStorage.getItem("authToken")}},
        (err, resp, body) => handleResponse(resp, () => {
            dispatch({type: ActionTypes.RECEIVE_REPOSITORIES, data: JSON.parse(body)});
            next();
        })
    );
};

const toggleRepositoryEnabled = (id, operation) => (dispatch) =>
    xhr({url: `/repositories/${id}/${operation}`, method: "PUT", headers: {'Authorization': localStorage.getItem("authToken")}},
        (err, resp, body) => handleResponse(resp));

const enableRepository = (id) => toggleRepositoryEnabled(id, "enable");

const disableRepository = (id) => toggleRepositoryEnabled(id, "disable");

const saveRepository = (next) => (dispatch, getState) => {
    const { underEdit } = getState().repositories;
    if (!underEdit) {
        return;
    }

    xhr({
        url: underEdit.id ? `/repositories/${underEdit.id}` : `/repositories`,
        method: underEdit.id ? "PUT" : "POST",
        headers: {
            'Content-type': "application/json",
            'Accept': 'application/json',
            'Authorization': localStorage.getItem("authToken")
        },
        body: JSON.stringify({
            ...underEdit,
            schedule: Schedule[underEdit.schedule].enumValue
        })
    }, (err, resp, body) => handleResponse(resp, () => {
            dispatch({type: ActionTypes.ON_SAVE_REPOSITORY});
            next();
        })
    );
};


const validateNewRepository = (repository) => (dispatch) =>
    xhr({
        url: `/repositories/validate`,
        method: "POST",
        headers: {
            "Content-type": "application/json",
            "Accept": "application/json",
            'Authorization': localStorage.getItem("authToken")
        },
        body: JSON.stringify({
            set: repository.set,
            url: repository.url,
            metadataPrefix: repository.metadataPrefix
        })
    }, (err, resp, body) => {
        if (resp.statusCode > 299) {
            dispatch({
                type: ActionTypes.RECEIVE_NEW_REPOSITORY_VALIDATION_RESULTS,
                underEdit: repository,
                data: {
                    urlIsValidOAI: false,
                    setExists: undefined,
                    metadataFormatSupported: undefined
                }
            })
        } else {
            dispatch({
                type: ActionTypes.RECEIVE_NEW_REPOSITORY_VALIDATION_RESULTS,
                underEdit: repository,
                data: {
                    ...JSON.parse(body),
                    urlIsValidOAI: true
                }
            })
        }
    });

const deleteRepository = (id, next = () => {}) => (dispatch) => {
    xhr({
        url: `/repositories/${id}`,
        method: "DELETE",
        headers: {'Authorization': localStorage.getItem("authToken")}
    }, (err, resp, body) => handleResponse(resp, next));
};

export {
    fetchRepositories,
    enableRepository,
    disableRepository,
    validateNewRepository,
    saveRepository,
    deleteRepository
};
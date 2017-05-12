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

const saveRepository = (next) => (dispatch, getState) => {
    const { underEdit } = getState().repositories;
    if (!underEdit) {
        return;
    }

    xhr({
        url: underEdit.id ? `/repositories/${underEdit.id}` : `/repositories`,
        method: underEdit.id ? "PUT" : "POST",
        headers: { 'Content-type': "application/json", 'Accept': 'application/json'},
        body: JSON.stringify(underEdit)
    }, (err, resp, body) => {
        const savedRepository = JSON.parse(body);
        next(savedRepository.id)
    });
};


const validateNewRepository = (repository) => (dispatch) =>
    xhr({
        url: `/repositories/validate`,
        method: "POST",
        headers: { "Content-type": "application/json", "Accept": "application/json" },
        body: JSON.stringify(repository)
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

export {
    fetchRepositories,
    enableRepository,
    disableRepository,
    setHarvestSchedule,
    validateNewRepository,
    saveRepository
};
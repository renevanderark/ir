import xhr from "xhr";

import {
    enableRepository,
    disableRepository,
    setHarvestSchedule,
    validateNewRepository,
    saveRepository,
    fetchRepositories,
    deleteRepository
} from "./actions/repositories";

import {
    startHarvest,
    interruptHarvest
} from "./actions/harvesters";

const startOaiRecordFetcher = () => (dispatch) =>
    xhr({url: "/workers/start", "method": "PUT"}, (err, resp, body) => {});

const disableOaiRecordFetcher = () => (dispatch) =>
    xhr({url: "/workers/disable", "method": "PUT"}, (err, resp, body) => {});

export default function actionsMaker(navigateTo, dispatch) {
    return {
        onEnableRepository: (id) => dispatch(enableRepository(id)),
        onDisableRepository: (id) => dispatch(disableRepository(id)),
        onSetSchedule: (id, scheduleEnumValue) => dispatch(setHarvestSchedule(id, scheduleEnumValue)),
        onValidateNewRepository: (repository) => dispatch(validateNewRepository(repository)),
        onStartHarvest: (repositoryId) => dispatch(startHarvest(repositoryId)),
        onInterruptHarvest: (repositoryId) => dispatch(interruptHarvest(repositoryId)),
        onSaveRepository: () => dispatch(saveRepository(() => navigateTo("root"))),
        onDeleteRepository: (id) => dispatch(deleteRepository(id, () => navigateTo("root", []))),
        onRefetchRepositories: () => dispatch(fetchRepositories(() => {})),

        onStartOaiRecordFetcher: () => dispatch(startOaiRecordFetcher()),
        onDisableOaiRecordFetcher: () => dispatch(disableOaiRecordFetcher()),
    };
}
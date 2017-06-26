import {RunState} from "../etc/enums";
export default (state, routed) => {
    const { repositoryId } = routed.params;
    const { errorStatus, recordStatus, list: repositoryList, harvesterRunStates } =  state.repositories;
    const { statusCodes: { errorStatuses }} = state.status;

    return {
        errorStatus: Object.keys(errorStatus[repositoryId] || {}).map(errorCode => ({
            code: errorCode,
            statusText: errorStatuses[errorCode],
            amount: errorStatus[repositoryId][errorCode]
        })),
        recordStatus: recordStatus[repositoryId] || {},
        repository: repositoryList.find(repo => repo.id === parseInt(repositoryId, 10)),
        runState: harvesterRunStates[repositoryId] || { runState : RunState.WAITING}
    };
}
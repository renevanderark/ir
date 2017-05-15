import {
    enableRepository,
    disableRepository,
    setHarvestSchedule,
    validateNewRepository,
    saveRepository,
    fetchRepositories
} from "./actions/repositories";

export default function actionsMaker(navigateTo, dispatch) {
    return {
        onEnableRepository: (id) => dispatch(enableRepository(id)),
        onDisableRepository: (id) => dispatch(disableRepository(id)),
        onSetSchedule: (id, scheduleEnumValue) => dispatch(setHarvestSchedule(id, scheduleEnumValue)),
        onValidateNewRepository: (repository) => dispatch(validateNewRepository(repository)),

        onSaveRepository: () => dispatch(saveRepository(() => navigateTo("root"))),
        onRefetchRepositories: () => dispatch(fetchRepositories(() => {}))
    };
}
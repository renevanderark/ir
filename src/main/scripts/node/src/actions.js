import {
    enableRepository,
    disableRepository,
    setHarvestSchedule
} from "./actions/repositories";

export default function actionsMaker(navigateTo, dispatch) {
    return {
        onEnableRepository: (id) => dispatch(enableRepository(id)),
        onDisableRepository: (id) => dispatch(disableRepository(id)),
        onSetSchedule: (id, scheduleEnumValue) => dispatch(setHarvestSchedule(id, scheduleEnumValue))
    };
}
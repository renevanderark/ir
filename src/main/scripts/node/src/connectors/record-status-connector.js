export default (state, routed) => ({
    kbObjId: routed.params.kbObjId,
    repositories: state.repositories.list,
    record: (state.records.record || {}).record,
    errorReport: (state.records.record || {}).errorReport,
    processStatuses: state.status.statusCodes.processStatuses,
    errorStatuses: state.status.statusCodes.errorStatuses

})
export default (state, routed) => ({
    kbObjId: routed.params.kbObjId,
    record: (state.records.record || {}).record,
    errorReport: (state.records.record || {}).errorReport,
    processStatuses: state.status.statusCodes.processStatuses
})
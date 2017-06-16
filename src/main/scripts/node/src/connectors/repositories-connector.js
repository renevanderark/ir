export default (state) => ({
    repositories: state.repositories,
    fetcherRunstate: state.fetcherRunstate,
    recordList: state.records.list,
    processStatuses: state.status.statusCodes.processStatuses
});
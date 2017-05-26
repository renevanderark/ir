export default (state, routed) => ({
    errorStatus: state.repositories.errorStatus[routed.params.repositoryId] || {},
    recordStatus: state.repositories.recordStatus[routed.params.repositoryId] || {},
    repository: state.repositories.list.find(repo => repo.id === parseInt(routed.params.repositoryId, 10))
});
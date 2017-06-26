const urls = {
    root() {
        return "/";
    },
    newRepository() {
        return "/nieuw"
    },
    editRepository(id) {
        return id
            ? `/bewerken/${id}`
            : "/bewerken/:repositoryId"
    },
    repositoryStatus(id) {
        return id
            ? `/overzicht/${id}`
            : "/overzicht/:repositoryId"
    },
    record(kbObjId) {
        return kbObjId
            ? `/publicatie/${kbObjId}`
            : "/publicatie/:kbObjId"
    }
};

export { urls };
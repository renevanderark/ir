export default (state) => ({
    underEdit: state.repositories.underEdit || {
        name: "",
        url: "",
        set: "",
        metadataPrefix: "",
        dateStamp: null
    },
    validationResultsUnderEdit: state.repositories.validationResultsUnderEdit || {}
});
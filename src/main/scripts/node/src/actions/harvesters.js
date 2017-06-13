import xhr from "xhr";
const startHarvest = (repositoryId) => (distpatch) =>
    xhr({
        method: "POST",
        headers: {
          'Accept': "application/json",
          'Authorization': localStorage.getItem("authToken")
        },
        uri: `/harvesters/${repositoryId}/start`
    }, (err, resp, body) => { });

const interruptHarvest = (repositoryId) => (distpatch) =>
    xhr({
        method: "POST",
        headers: {
            'Accept': "application/json",
            'Authorization': localStorage.getItem("authToken")
        },
        uri: `/harvesters/${repositoryId}/interrupt`
    }, (err, resp, body) => { });

export { startHarvest, interruptHarvest }
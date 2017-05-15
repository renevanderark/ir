import xhr from "xhr";
const startHarvest = (repositoryId) => (distpatch) =>
    xhr({
        method: "POST",
        headers: {
          'Accept': "application/json"
        },
        uri: `/harvesters/${repositoryId}/start`
    }, (err, resp, body) => console.log(err, resp, body));

const interruptHarvest = (repositoryId) => (distpatch) =>
    xhr({
        method: "POST",
        headers: {
            'Accept': "application/json"
        },
        uri: `/harvesters/${repositoryId}/interrupt`
    }, (err, resp, body) => console.log(err, resp, body));

export { startHarvest, interruptHarvest }
import xhr from "xhr";
import {handleResponse} from "./response-handler";
const startHarvest = (repositoryId) => (distpatch) =>
    xhr({
        method: "POST",
        headers: {
          'Accept': "application/json",
          'Authorization': localStorage.getItem("authToken")
        },
        uri: `/harvesters/${repositoryId}/start`
    }, (err, resp, body) => {
        handleResponse(resp);
    });

const interruptHarvest = (repositoryId) => (distpatch) =>
    xhr({
        method: "POST",
        headers: {
            'Accept': "application/json",
            'Authorization': localStorage.getItem("authToken")
        },
        uri: `/harvesters/${repositoryId}/interrupt`
    }, (err, resp, body) => {
        handleResponse(resp);
    });

export { startHarvest, interruptHarvest }
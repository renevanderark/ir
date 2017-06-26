import xhr from "xhr";
import {handleResponse} from "./response-handler";
import ActionTypes from "./action-types";


const fetchCredentials = () => (dispatch) => {
    xhr({url: `/authenticate/me`, method: "GET", headers: {'Authorization': localStorage.getItem("authToken")}},
        (err, resp, body) => handleResponse(resp, () => {
            dispatch({type: ActionTypes.RECEIVE_CREDENTIALS, data: JSON.parse(body)});
        })
    );
};

export {fetchCredentials};
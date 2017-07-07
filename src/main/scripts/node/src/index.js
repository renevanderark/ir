import "./polyfills"

import React from "react";
import ReactDOM from "react-dom";
import router from "./router";

import { authenticateAndInitialize } from "./bootstrap";
import {fetchInitialData} from "./actions/fetch-initial-data";

// Authenticate, initialize render data and then render the app to the DOM, registering the router
authenticateAndInitialize(() => ReactDOM.render(router, document.getElementById("app")));

setInterval(
    () => fetchInitialData(() => console.log(`Force reload at ${new Date().getTime()}`)),
    300 * 1000
);
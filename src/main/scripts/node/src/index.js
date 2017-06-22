import "./polyfills"

import React from "react";
import ReactDOM from "react-dom";
import router from "./router";

import { authenticateAndInitialize } from "./bootstrap";

authenticateAndInitialize(() => {
    ReactDOM.render(router, document.getElementById("app"))
});
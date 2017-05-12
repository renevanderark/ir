import "./polyfills"
import React from "react";
import ReactDOM from "react-dom";
import router from "./router";
import {connectSocket, fetchInitialData } from "./bootstrap";

fetchInitialData(() => ReactDOM.render(router, document.getElementById("app")));
connectSocket();
import "./polyfills"
import React from "react";
import ReactDOM from "react-dom";
import router from "./router";
import bootstrap from "./bootstrap";

bootstrap(() => ReactDOM.render(router, document.getElementById("app")));
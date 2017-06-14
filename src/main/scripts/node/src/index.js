import "./polyfills"
import React from "react";
import ReactDOM from "react-dom";
import router from "./router";
import {connectSocket, fetchInitialData } from "./bootstrap";

const tokenFromUrl = () => {
    return location.href.indexOf("token=") < 0 ?
        null :
        location.href.replace(/^.+token=/, "");
};

const token = localStorage.getItem("authToken");
const urlToken = tokenFromUrl();

if (urlToken !== null) {
    localStorage.setItem("authToken", urlToken);
    location.href = "/";
} else if (token === null) {
    location.href = `${globals.kbAutLocation}?id=dare2&application=ir-objectharvester&return_url=` +
            encodeURIComponent(`http://${globals.hostName}/authenticate`);
} else {
    localStorage.setItem("authToken", token);
    fetchInitialData(() => ReactDOM.render(router, document.getElementById("app")));
    connectSocket();
}
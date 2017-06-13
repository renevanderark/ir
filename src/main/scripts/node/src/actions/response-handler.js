const handleResponse = (resp, next = () => {}) => {
    if (resp.statusCode > 400) {
        localStorage.removeItem("authToken");
        location.href = "/authenticate";
    } else {
        next();
    }
};

export { handleResponse }
const rootConnector = (state) => ({

    socketClosed: state.status.socketClosed,
    username: state.status.credentials.username
});

export default rootConnector;
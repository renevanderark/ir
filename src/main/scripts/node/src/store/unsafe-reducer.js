const initialState = {
    data: [],
    error: false
};

export default function(state=initialState, action) {
    switch (action.type) {
        case "RECEIVE_UNSAFE_RESPONSE":
            return {
                data: action.data,
                error: false
            };
        case "RECEIVE_UNSAFE_ERROR":
            return {
                data: [],
                error: true
            };
        default:
    }

    return state;
}
import React from "react";

class RepositoryStatus extends React.Component {

    render() {
        return (
            <pre>
                {JSON.stringify(this.props, null, 2)}
            </pre>
        );
    }
}

export default RepositoryStatus;
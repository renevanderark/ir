import React from "react";

import { Link } from "react-router";

class Header extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.socketClosed !== nextProps.socketClosed;
    }

    render() {
        const { socketClosed } = this.props;

        return (
            <div className="navbar navbar-default">
                <div className="container container-fluid">
                    <div className="navbar-brand">
                        <Link to="/">Dare 2</Link>
                    </div>
                    <div className="navbar-right navbar-text">
                        <span style={{transform: "rotate(90deg)", color: socketClosed ? "red" : "green"}}
                              className="glyphicon glyphicon-transfer" />
                    </div>
                </div>
            </div>
        );
    }
}

export default Header;
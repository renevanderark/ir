import React from "react";

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
                        Dare 2
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
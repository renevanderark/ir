import React from "react";
import PropTypes from "prop-types";

class Panel extends React.Component {

    render() {
        const { title } = this.props;

        return (
            <div className={`panel panel-default`}>
                <div className="panel-heading">
                    {title}
                </div>
                <div className="panel-body">
                    {this.props.children}
                    <div className="clearfix" />
                </div>
            </div>
        );
    }
}

Panel.propTypes = {
    title: PropTypes.string.isRequired
};

export default Panel;
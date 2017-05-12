import React from "react";
import { Link } from "react-router";


class BreadCrumbs extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.titles.join(",") !== nextProps.titles.join(",");
    }

    render() {
        return (
            <ol className="breadcrumb">
                <li><Link to="/">Dashboard</Link></li>
                {this.props.titles.map((title, i) => (
                    <li key={i} className={i === this.props.titles.length - 1 ? "active" : ""}>
                        {title}
                    </li>
                ))}
            </ol>
        );
    }
}

BreadCrumbs.defaultProps = {
    titles: []
};

BreadCrumbs.propTypes = {
    titles: React.PropTypes.array
};

export default BreadCrumbs;
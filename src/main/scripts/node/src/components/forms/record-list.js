import React from "react";
import { Link } from "react-router";
import { urls } from "../../router";

class RecordList extends React.Component {

    render() {
        const { selectedIndex, processStatuses } = this.props;
        return (
            <ul style={{position: "absolute", backgroundColor: "#fff", zIndex: 999, width: "100%"}}
                className="list-group">
                {this.props.recordList.map((record, i) => (
                    <li className={`list-group-item ${selectedIndex === i ? "active" : ""}`}
                        onMouseOver={() => this.props.onHover(i)}
                        key={i}>
                        <Link to={urls.record(record.kbObjId)}>
                            {record.kbObjId} -{" "}
                            {processStatuses[record.state]} -{" "}
                            ({this.props.repositories.find(repo => repo.id === record.repositoryId).name})
                        </Link>
                    </li>
                ))}
            </ul>
        );
    }
}

export default RecordList;
import React from "react";

class RecordStatus extends React.Component {

    componentWillReceiveProps(nextProps) {
        const { onFetchRecord } = this.props;

        if (nextProps.kbObjId !== this.props.kbObjId) {
            onFetchRecord(nextProps.kbObjId);
        }
    }

    componentDidMount() {
        const {record, kbObjId, onFetchRecord } = this.props;

        if (!record || record.kbObjId !== kbObjId) {
            onFetchRecord(kbObjId);
        }
    }


    render() {
        return (
            <pre>
                {JSON.stringify(this.props, null, 2)}
            </pre>
        );
    }
}

export default RecordStatus;
import React from "react";
import RecordList from "./record-list";


class FindField extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            query: "",
            selectedIndex: 0
        }
    }

    onChange(ev) {
        this.setState({query: ev.target.value, selectedIndex: 0});

        if (ev.target.value.length > 0) {
            this.props.onFindRecords(ev.target.value);
        } else {
            this.props.onClearFoundRecords();
        }
    }

    onBlur() {
        this.setState({query: ""});

        setTimeout(this.props.onClearFoundRecords, 250);
    }

    onKeyPress(ev) {
        if (ev.key === 'Enter') {
            if (this.props.recordList.length > this.state.selectedIndex) {
                this.props.onNavigateTo("record", [this.props.recordList[this.state.selectedIndex].ipName]);
                this.props.onClearFoundRecords();
            }
        } else if (ev.key === 'ArrowUp') {
            const newSelected = this.state.selectedIndex - 1 < 0
                ? this.props.recordList.length - 1 < 0 ? 0 : this.props.recordList.length - 1
                : this.state.selectedIndex - 1;

            this.setState({selectedIndex: newSelected});
        } else if (ev.key === 'ArrowDown') {
            const newSelected = this.state.selectedIndex + 1 >= this.props.recordList.length
                ? 0
                : this.state.selectedIndex + 1;

            this.setState({selectedIndex: newSelected});
        }
    }

    render() {
        const { query, selectedIndex } = this.state;

        return (
            <div>
                <input className="form-control" type="text" value={query}
                       onBlur={this.onBlur.bind(this)}
                       onChange={this.onChange.bind(this)}
                       onKeyDown={this.onKeyPress.bind(this)}
                />
                <RecordList
                    selectedIndex={selectedIndex}
                    onHover={(idx) => this.setState({selectedIndex: idx})}
                    processStatuses={this.props.processStatuses}
                    repositories={this.props.repositories}
                    recordList={this.props.recordList} />
            </div>
        );
    }
}

export default FindField;
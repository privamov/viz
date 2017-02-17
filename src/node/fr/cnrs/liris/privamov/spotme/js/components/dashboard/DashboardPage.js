const React = require('react');
const moment = require('moment');

const LocationCalendarContainer = require('./LocationCalendarContainer');
const LocationMapContainer = require('./LocationMapContainer');
const TransformChooser = require('./TransformChooser');

let LocationPage = React.createClass({
  getInitialState: function () {
    return {
      transform: null,
      day: moment()
    };
  },

  getDefaultProps: function () {
    return {
      dataset: window.Viz.dataset,
    };
  },

  _handleCalendarChange: function (currentDate) {
    this.setState({day: currentDate});
  },

  _handleTransformChange: function (type, param) {
    const transform = type ? {type: type, param: param} : null;
    this.setState({transform: transform});
  },

  render: function () {
    return (
      <div className="container-fluid">
        <div className="row">
          <div className="col-sm-2">
            <LocationCalendarContainer
              {...this.props}
              accessToken={this.props.params.token}
              onChange={this._handleCalendarChange}/>
            <hr/>
            <TransformChooser onChange={this._handleTransformChange}/>
          </div>
          <div className="col-sm-10">
            <LocationMapContainer
              {...this.props}
              accessToken={this.props.params.token}
              day={this.state.day}
              transform={this.state.transform}/>
          </div>
        </div>
      </div>
    );
  }
});

LocationPage.propTypes = {
  dataset: React.PropTypes.string.isRequired,
};

module.exports = LocationPage;
const React = require('react');

let LoginPage = React.createClass({
  getInitialState: function() {
    return {
      token: '',
    };
  },

  _handleChange: function(e) {
    this.setState({token: e.target.value})
  },

  _handleSubmit: function (e) {
    e.nativeEvent.preventDefault()
    this.props.router.push('/view/' + this.state.token)
  },

  render: function () {
    return (
      <div className="container">
        <form onSubmit={this._handleSubmit}>
          <div className="form-group">
            <label>Access token</label>
            <input type="text" className="form-control" value={this.state.token} onChange={this._handleChange}/>
          </div>
          <button type="submit" className="btn btn-primary">Login</button>
        </form>
      </div>
    );
  }
});

module.exports = LoginPage;
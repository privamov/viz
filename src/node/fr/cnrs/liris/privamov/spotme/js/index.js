var React = require('react');
var ReactDOM = require('react-dom');
var Router = require('react-router').Router;
var Route = require('react-router').Route;
var IndexRoute = require('react-router').IndexRoute;
var hashHistory = require('react-router').hashHistory;
var _ = require('lodash');

var App = require('./components/App');
var DashboardPage = require('./components/dashboard/DashboardPage');
var LoginPage = require('./components/login/LoginPage');

ReactDOM.render((
  <Router history={hashHistory}>
    <Route path="/" component={App}>
      <IndexRoute component={LoginPage}/>
      <Route path="view/:token" component={DashboardPage}/>
    </Route>
  </Router>
), document.getElementById('body'));
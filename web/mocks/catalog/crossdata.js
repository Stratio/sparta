const crossdata = {
  path: '/crossdata/queries',
  method: 'POST',
  template: {
    exception: 'ERROR: query failed'
  },
  status: (req, res, next) => {
    res.status(500);
    next();
  }
};

module.exports = [crossdata];

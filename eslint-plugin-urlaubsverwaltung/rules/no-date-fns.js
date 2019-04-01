const forbiddenFunctions = [
  'format',
  'startOfWeek',
];

function getValue (node) {
  if (node && node.source && node.source.value) {
    return node.source.value.trim ();
  }
  return '';
}

function getSpecifiers (node) {
  return (node && node.specifiers) || [];
}

function checkAndReport (context, node, value) {
  if (value.startsWith('date-fns')) {
    getSpecifiers (node)
      .filter (n => n.type !== 'ImportDefaultSpecifier')
      .map (n => n.imported.name)
      .filter (func => forbiddenFunctions.includes (func))
      .forEach (func => {
        context.report ({
          node,
          message: `please use ${func} function from our own libs/date-fns package`,
        });
      });
    getSpecifiers (node)
      .filter (n => n.type === 'ImportDefaultSpecifier')
      .map (n => n.local.name)
      .forEach (func => {
        context.report ({
          node,
          message: `date-fns (imported as '${func}') should not be used directly`,
        });
      });
  }
}

function handleImports (context) {
  return node => {
    const value = getValue (node);
    if (value) {
      checkAndReport (context, node, value);
    }
  };
}


module.exports = {
  meta: {
    // eslint-disable-next-line unicorn/prevent-abbreviations
    docs: {
      description: 'enforce special urlaubsverwaltung date-fns function usage over direct import',
      category   : 'frontend architecture',
      recommended: true,
    },
    schema: [],
  },

  create (context) {
    return {
      ImportDeclaration: handleImports (context),
    };
  },
};

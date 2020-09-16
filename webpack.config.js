const path = require('path');
const webpack = require('webpack');
const WebpackAssetsManifest = require('webpack-assets-manifest');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const TerserJSPlugin = require("terser-webpack-plugin");
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");

const isProduction = process.env.NODE_ENV === 'production';

const paths = {
  src: `./src/main/javascript`,
};

module.exports = {
  devtool: false,

  entry: {
    polyfill: '@babel/polyfill',
    'date-fns-localized': `${paths.src}/bundles/date-fns-localized.js`,
    app_detail: `${paths.src}/bundles/app-detail.js`,
    app_form: `${paths.src}/bundles/app-form.js`,
    app_list: `${paths.src}/bundles/app-list.js`,
    app_statistics: `${paths.src}/bundles/app-statistics.js`,
    common: `${paths.src}/bundles/common.js`,
    person_overview: `${paths.src}/bundles/person-overview.js`,
    person_form: `${paths.src}/bundles/person-form.js`,
    sick_note_form: `${paths.src}/bundles/sick-note-form.js`,
    sick_note: `${paths.src}/bundles/sick-note.js`,
    sick_note_convert: `${paths.src}/bundles/sick-note-convert.js`,
    sick_notes: `${paths.src}/bundles/sick-notes.js`,
    person_view: `${paths.src}/bundles/person-view.js`,
    absences_overview: `${paths.src}/bundles/absences-overview.js`,
    department_form: `${paths.src}/bundles/department-form.js`,
    department_list: `${paths.src}/bundles/department-list.js`,
    overtime_overview: `${paths.src}/bundles/overtime-overview.js`,
    overtime_form: `${paths.src}/bundles/overtime-form.js`,
    settings_form: `${paths.src}/bundles/settings-form.js`,
    account_form: `${paths.src}/bundles/account-form.js`,
    workingtime_form: `${paths.src}/bundles/workingtime-form.js`,
    copy_to_clipboard_input: `${paths.src}/components/copy-to-clipboard-input/index.js`,
    tabs: `${paths.src}/components/tabs/index.js`,
  },

  output: {
    path: path.resolve(__dirname, 'target/classes/static/assets'),
    filename: isProduction ? '[name].[contenthash].min.js' : '[name].js',
    publicPath: '/assets/'
  },

  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: 'babel-loader'
      },
      {
        test: /\.css$/,
        use: [
          MiniCssExtractPlugin.loader,
          "css-loader",
        ],
      },
      {
        test: /\.less$/,
        use: [
          MiniCssExtractPlugin.loader,
          "css-loader",
          "less-loader",
        ],
      },
      {
        test: /\.(png|jpg|jpeg|gif)$/i,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 8192,
            }
          }
        ]
      }
    ]
  },

  plugins: [
    // https://webpack.js.org/guides/caching/#module-identifiers
    // include HashedModuleIdsPlugin so that file hashes don't change unexpectedly
    new webpack.HashedModuleIdsPlugin(),
    // This Webpack plugin will generate a JSON file that matches
    // the original filename with the hashed version.
    // This file is read by the taglib AssetsHashResolverTag.java to ease asset handling in templates
    new WebpackAssetsManifest({
      // output path is relative to webpack.output.path
      output: path.resolve(__dirname, 'src/main/webapp/WEB-INF/assets-manifest.json'),
      publicPath: true,
    }),
    new webpack.ProvidePlugin({
      $: 'jquery',
      jQuery: 'jquery',
    }),
    new MiniCssExtractPlugin({
      filename: isProduction ? "../assets/[name].[contenthash].css" : "../assets/[name].css",
    })
  ],

  optimization: {
    runtimeChunk: 'single',
    // always create named chunkIds to have deterministic builds.
    // default would be 'natural' which uses numeric ids in order of (import) usage.
    //    changing the first import usage results in changing ALL chunk ids independently of the actual chunk content
    //    which reverses the content hash usage in the filename for long-term-caching. (the content hash changes when
    //    the chunkId changes)
    chunkIds: 'named',
    splitChunks: {
      chunks: 'all',
      maxInitialRequests: Infinity,
      minSize: 1024,
      cacheGroups: {
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name(module) {
            // get the name. E.g. node_modules/packageName/not/this/part.js
            // or node_modules/packageName
            const packageName = module.context.match(/[\\/]node_modules[\\/](.*?)([\\/]|$)/)[1];

            if (/node_modules\/jquery-ui\/ui\/i18n/.test(module.context)) {
              const locale = module.resource.match(/datepicker-(\w\w)/)[1];
              // build separate bundles for jquery-ui-datepicker
              // which can be included on demand in the view templates
              // or used as dynamic import and handled by webpack
              return `npm.${packageName}.datepicker.${locale}`;
            }

            if (packageName === 'date-fns') {
              // build separate bundles for dateFn locales
              // which can be included on demand in the view templates
              // or used as dynamic import and handled by webpack
              const dateFnLocaleMatch = module.context.match(/node_modules\/date-fns\/esm\/locale\/((?!en)(?!_)\w\w)/);
              if (dateFnLocaleMatch) {
                const locale = dateFnLocaleMatch[1];
                return `npm.${packageName}.${locale}`;
              }
            }

            // npm package names are URL-safe, but some servers don't like @ symbols
            return `npm.${packageName.replace('@', '')}`;
          },
        },
      },
    },
    minimizer: [
      new TerserJSPlugin({}),
      new OptimizeCSSAssetsPlugin({})
    ]
  },
};

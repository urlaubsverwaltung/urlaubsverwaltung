const path = require('path');
const webpack = require('webpack');
const WebpackAssetsManifest = require('webpack-assets-manifest');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const TerserJSPlugin = require("terser-webpack-plugin");
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");

const isProduction = process.env.NODE_ENV === 'production';

module.exports = {
  devtool: false,

  entry: {
    polyfill: '@babel/polyfill',
    'date-fns-localized': './src/main/webapp/bundles/date-fns-localized.js',
    login: './src/main/webapp/bundles/login.js',
    app_detail: './src/main/webapp/bundles/app-detail.js',
    app_form: './src/main/webapp/bundles/app-form.js',
    app_list: './src/main/webapp/bundles/app-list.js',
    app_statistics: './src/main/webapp/bundles/app-statistics.js',
    common: './src/main/webapp/bundles/common.js',
    person_overview: './src/main/webapp/bundles/person-overview.js',
    person_form: './src/main/webapp/bundles/person-form.js',
    sick_note_form: './src/main/webapp/bundles/sick-note-form.js',
    sick_note: './src/main/webapp/bundles/sick-note.js',
    sick_note_convert: './src/main/webapp/bundles/sick-note-convert.js',
    sick_notes: './src/main/webapp/bundles/sick-notes.js',
    person_view: './src/main/webapp/bundles/person-view.js',
    vacation_overview: './src/main/webapp/bundles/vacation-overview.js',
    department_form: './src/main/webapp/bundles/department-form.js',
    department_list: './src/main/webapp/bundles/department-list.js',
    overtime_form: './src/main/webapp/bundles/overtime-form.js',
    settings_form: './src/main/webapp/bundles/settings-form.js',
    account_form: './src/main/webapp/bundles/account-form.js',
    workingtime_form: './src/main/webapp/bundles/workingtime-form.js',
    copy_to_clipboard_input: './src/main/webapp/components/copy-to-clipboard-input/index.js',
    tabs: './src/main/webapp/components/tabs/index.js',
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
        test: /\.(woff(2)?|ttf|eot|svg)$/,
        use: [{
          loader: 'file-loader',
          options: {
            name: '[name].[ext]',
            outputPath: 'fonts/'
          }
        }]
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
              const dateFnLocaleMatch = module.context.match(/node_modules\/date-fns\/locale\/((?!en)(?!_)\w\w)/);
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

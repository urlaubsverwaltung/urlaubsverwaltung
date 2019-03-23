# Heroku - integration
* [using the cli](#using-the-cli)
* [configuration files](#configuration-files)

Heroku will build all pull requests and will execute it on free dynos.
That enables you to check directly if a pull request works.

## using the cli

See how to install the [command line interface](https://devcenter.heroku.com/articles/heroku-cli) and
how to [get started](https://devcenter.heroku.com/articles/heroku-cli#getting-started) with 
[heroku](https://devcenter.heroku.com)

```bash
# show running apps
heroku apps

# to see the free dyno quota
heroku ps -a urlaubsverwaltung

# to see the logs of a dyno
heroku logs -a urlaubsverwaltung --tail
# to see the logs of a pull request 510
heroku logs -a urlaubsverwaltung-pr-510 --tail
```

# eslint-plugin-urlaubsverwaltung

custom eslint rules for our application

## Rules

### no-date-fns

some date-fns functions like `format` are wrapped by us with special behaviour.
for instance internationalisation or date patterns.

valid:

```js
import format from '../lib/date-fns/format';
import startOfWeek from '../lib/date-fns/startOfWeek';
```

invaild:

```js
import { format } from 'date-fns';
import { startOfWeek } from 'date-fns';
import DateFns from 'date-fns';
```

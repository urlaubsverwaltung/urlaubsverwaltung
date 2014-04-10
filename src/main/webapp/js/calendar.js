// TODO half holidays
// TODO responsivness (various widths, browsers, os, ...)
// TODO description of day colors

$(function() {

    var $datepicker = $('#datepicker');

    var keyCodes = {
        escape: 27
    };

    var mouseButtons = {
        left   : 0,
        middle : 1,
        right  : 2
    };

    var CSS = {
        day                : 'datepicker-day',
        daySelected        : 'datepicker-day-selected',
        dayToday           : 'datepicker-day-today',
        dayWeekend         : 'datepicker-day-weekend',
        dayPast            : 'datepicker-day-past',
        dayPublicHoliday   : 'datepicker-day-public-holiday',
        dayPersonalHoliday : 'datepicker-day-personal-holiday',
        dayHalfHoliday     : 'datepicker-day-half-holiday',
        next               : 'datepicker-next',
        prev               : 'datepicker-prev',
        month              : 'datepicker-month',
        monthNext          : 'datepicker-month-next',
        monthPrev          : 'datepicker-month-prev',
        mousedown          : 'mousedown'
    };

    var DATA = {
        date       : 'datepickerDate',
        month      : 'datepickerMonth',
        year       : 'datepickerYear',
        selected   : 'datepickerSelected',
        selectFrom : 'datepickerSelectFrom',
        selectTo   : 'datepickerSelectTo',
        selectable : 'datepickerSelectable'
    };


    var Assertion = (function() {

        var holidayService;

        var assert = {
            isToday: function(date) {
                return date.format('DD.MM.YY') === moment().format('DD.MM.YY');
            },
            isWeekend: function(date) {
                return date.day() === 0 || date.day() === 6;
            },
            isPast: function(date) {
                return date.isBefore( moment() );
            },
            isPublicHoliday: function(date) {
                return holidayService.isPublicHoliday(date);
            },
            isPersonalHoliday: function(date) {
                return holidayService.isPersonalHoliday(date);
            },
            isHalfHoliday: function(date) {
                // TODO check booked holidays from user
                // hardcoded 24.12. and 31.12. ... don't know...
                return date.month() === 11 && (date.date() === 24 || date.date() === 31);
            }
        };

        return {
            create: function(_holidayService) {
                holidayService = _holidayService;
                return assert;
            }
        };

    }());


    var HolidayService = (function() {

        var _CACHE  = {};

        var urlPrefix;

        function paramize(p) {
            var result = '?';
            for (var v in p) {
                if (p[v]) {
                    result += v + '=' + p[v] + '&';
                }
            }
            return result.replace(/[?&]$/, '');
        }

        /**
         *
         * @param {string} endpoint
         * @param {{}} params
         * @returns {$.ajax}
         */
        function fetch(endpoint, params) {

            var query = endpoint + paramize(params);

            return $.ajax({
                url: urlPrefix + '/calendar/' + query,
                dataType: 'json'
            });
        }

        function cacheData(type) {
            var c = _CACHE[type] = _CACHE[type] || {};
            return function(data) {
                $.each(data, function(idx, d) {
                    var y = d.match(/\d{0,4}/)[0];
                    c[y] = c[y] || [];
                    c[y].push(d);
                });
            }
        }

        function isHoliday(type) {
            return function(date) {
                var y = date.year();
                return _CACHE[type][y] && _CACHE[type][y].indexOf( date.format('YYYY-MM-DD') ) > -1;
            };
        }

        var HolidayService = {

            isPersonalHoliday: isHoliday('holiday'),

            isPublicHoliday: isHoliday('publicHoliday'),

            /**
             *
             * @param {number} year
             * @returns {$.ajax}
             */
            fetchPublic: function(year) {

                var deferred = $.Deferred();

                _CACHE['publicHoliday'] = _CACHE['publicHoliday'] || {};

                if (_CACHE['publicHoliday'][year]) {
                    return deferred.resolve( _CACHE[year] );
                }
                else {
                    return fetch('public-holiday', {year: year}).success( cacheData('publicHoliday') );
                }
            },

            /**
             *
             * @param {number} personId
             * @param {number} year
             * @param {number} [month]
             * @returns {$.ajax}
             */
            fetchPersonal: function(personId, year) {
                var deferred = $.Deferred();

                _CACHE['holiday'] = _CACHE['holiday'] || {};

                if (_CACHE['holiday'][year]) {
                    return deferred.resolve( _CACHE[year] );
                }
                else {
                    return fetch('holiday', {personId: personId, year: year}).success( cacheData('holiday') );
                }
            }
        };

        return {
            create: function(_urlPrefix) {
                urlPrefix = _urlPrefix;
                return HolidayService;
            }
        };

    }());


    var View = (function() {

        var assert;

        var TMPL = {

            container: '{{prevBtn}}<div class="datepicker-months-container">{{prevMonth}}{{months}}{{nextMonth}}</div>{{nextBtn}}',

            button: '<button class="{{css}}">{{text}}</button>',

            month: '<div class="datepicker-month {{css}}" data-datepicker-month="{{month}}" data-datepicker-year="{{year}}">{{title}}<table class="datepicker-table"><thead>{{weekdays}}</thead><tbody>{{weeks}}</tbody></table></div>',

            title: '<h3>{{title}}</h3>',

            // <tr><th>{{0}}</th>......<th>{{6}}</th></tr>
            weekdays: '<tr><th>{{' + [0,1,2,3,4,5,6].join('}}</th><th>{{') + '}}</th></tr>',

            // <tr><td>{{0}}</td>......<td>{{6}}</td></tr>
            week: '<tr><td>{{' + [0,1,2,3,4,5,6].join('}}</td><td>{{') + '}}</td></tr>',

            day: '<span class="datepicker-day {{css}}" data-datepicker-date="{{date}}" data-datepicker-selectable="{{selectable}}">{{day}}</span>'
        };

        function render(tmpl, data) {
            return tmpl.replace(/{{(\w+)}}/g, function(_, type) {

                if (typeof data === 'function') {
                    return data.apply(this, arguments);
                }

                var val = data[type];
                return typeof val === 'function' ? val() : val;
            });
        }

        function renderCalendar() {

            var date = moment();
            var monthsToShow = 4;

            return render(TMPL.container, {

                prevBtn   : renderButton ( CSS.prev, '&lt;&lt;'),
                nextBtn   : renderButton ( CSS.next, '&gt;&gt;'),
                prevMonth : renderMonth  ( moment(date).add('M', -1), CSS.monthPrev ),
                nextMonth : renderMonth  ( moment(date).add('M', monthsToShow + 1) , CSS.monthNext ),

                months: function() {
                    var html = '';
                    var d = moment(date);
                    while(monthsToShow--) {
                        html += renderMonth(d);
                        d.add('M', 1);
                    }
                    return html;
                }
            });
        }

        function renderButton(css, text) {
            return render(TMPL.button, {
                css : css,
                text: text
            });
        }

        function renderMonth(date, cssClasses) {

            var m = date.month();
            var d = moment(date);

            // first day of month
            d.date(1);

            return render(TMPL.month, {

                css     : cssClasses || '',
                month   : d.month(),
                year    : d.year(),
                title   : renderMonthTitle(d),
                weekdays: renderWeekdaysHeader(d),

                weeks: function() {
                    var html = '';
                    while(d.month() === m) {
                        html += renderWeek(d);
                        d.add('w', 1);
                        d.weekday(0);
                    }
                    return html;
                }
            });
        }

        function renderMonthTitle(date) {
            return render(TMPL.title, {
                title: date.format('MMMM YYYY')
            });
        }

        function renderWeekdaysHeader(date) {

            // 'de'   : 0 == Monday
            // 'en-ca': 0 == Sunday
            var d = moment(date).weekday(0);

            return render(TMPL.weekdays, {
                0: d.format('dd'),
                1: d.add('d', 1).format('dd'),
                2: d.add('d', 1).format('dd'),
                3: d.add('d', 1).format('dd'),
                4: d.add('d', 1).format('dd'),
                5: d.add('d', 1).format('dd'),
                6: d.add('d', 1).format('dd')
            });
        }

        function renderWeek(date) {

            var d = moment(date);
            var m = d.month();

            return render(TMPL.week, function(_, dayIdx) {

                var html = '&nbsp;';

                if (+dayIdx === d.weekday() && m === d.month()) {
                    html = renderDay(d);
                    d.add('d', 1);
                }

                return html;
            });
        }

        function renderDay(date) {

            function classes() {
                return [
                    assert.isToday           (date) ? CSS.dayToday           : '',
                    assert.isWeekend         (date) ? CSS.dayWeekend         : '',
                    assert.isPast            (date) ? CSS.dayPast            : '',
                    assert.isPublicHoliday   (date) ? CSS.dayPublicHoliday   : '',
                    assert.isPersonalHoliday (date) ? CSS.dayPersonalHoliday : '',
                    assert.isHalfHoliday     (date) ? CSS.dayHalfHoliday     : ''
                ].join(' ');
            }

            function isSelectable() {
                var isPast = assert.isPast(date);
                var isFutureHalf = assert.isHalfHoliday(date) && !isPast

                return isFutureHalf || !(
                    isPast ||
                    assert.isWeekend(date) ||
                    assert.isPublicHoliday(date) ||
                    assert.isPersonalHoliday(date)
                );
            }

            return render(TMPL.day, {
                date: date.format('YYYY-MM-DD'),
                day : date.format('DD'),
                css : classes(),
                selectable: isSelectable()
            });
        }

        var View = {

            display: function() {
                $datepicker.html( renderCalendar()).addClass('unselectable');
            },

            displayNext: function() {

                var elements = $datepicker.find('.' + CSS.month).get();
                var len      = elements.length;

                $(elements[0]).remove();
                $(elements[1]).addClass(CSS.monthPrev);

                var $lastMonth = $(elements[len - 1]);
                var month = +$lastMonth.data(DATA.month);
                var year  = +$lastMonth.data(DATA.year);

                var $nextMonth = $(renderMonth( moment().year(year).month(month).add('M', 1), CSS.monthNext ));

                $lastMonth.after($nextMonth).removeClass(CSS.monthNext);
            },

            displayPrev: function() {

                var elements = $datepicker.find('.' + CSS.month).get();
                var len = elements.length;

                $(elements[len - 1]).remove();
                $(elements[len - 2]).addClass(CSS.monthNext);

                var $firstMonth = $(elements[0]);
                var month = +$firstMonth.data(DATA.month);
                var year  = +$firstMonth.data(DATA.year);

                var $prevMonth = $(renderMonth( moment().year(year).month(month).subtract('M', 1), CSS.monthPrev ));

                $firstMonth.before($prevMonth).removeClass(CSS.monthPrev);
            }
        };

        return {
            create: function(_assert) {
                assert = _assert;
                return View;
            }
        };
    }());


    var Controller = (function() {

        var view;
        var holidayService;

        var datepickerHandlers = {

            mousedown: function(event) {

                if (event.button != mouseButtons.left) {
                    return;
                }

                $(document.body).addClass(CSS.mousedown);

                var dateThis = getDateFromEl(this);

                if ( !sameOrBetween(dateThis, selectionFrom(), selectionTo()) ) {

                    clearSelection();

                    $datepicker.data(DATA.selected, dateThis);

                    selectionFrom( dateThis );
                    selectionTo  ( dateThis );
                }
            },

            mouseup: function() {
                $(document.body).removeClass(CSS.mousedown);
            },

            mouseover: function() {
                if ( $(document.body).hasClass(CSS.mousedown) ) {

                    var dateThis     = getDateFromEl(this);
                    var dateSelected = $datepicker.data(DATA.selected);

                    var isThisBefore = dateThis.isBefore(dateSelected);

                    selectionFrom( isThisBefore ? dateThis     : dateSelected );
                    selectionTo  ( isThisBefore ? dateSelected : dateThis     );
                }
            },

            click: function() {
                
                var dateFrom = selectionFrom();
                var dateTo   = selectionTo  ();

                var dateThis = getDateFromEl(this);

                if (sameOrBetween(dateThis, dateFrom, dateTo)) {
                    bookHoliday(dateFrom, dateTo);
                }
            },

            clickNext: function() {

                var $month = $( $datepicker.find('.' + CSS.monthNext)[0] );

                // to load data for the new (invisible) prev month
                var date = moment()
                    .year ($month.data(DATA.year))
                    .month($month.data(DATA.month))
                    .add('M', 1);

                $.when(
                    holidayService.fetchPublic(date.year())
//                    , Holidays.fetchPersonal()
                ).then(view.displayNext);
            },

            clickPrev: function() {

                var $month = $( $datepicker.find('.' + CSS.monthPrev)[0] );

                // to load data for the new (invisible) prev month
                var date = moment()
                    .year ($month.data(DATA.year))
                    .month($month.data(DATA.month))
                    .subtract('M', 1);

                $.when(
                    holidayService.fetchPublic(date.year())
//                    , Holidays.fetchPersonal()
                ).then(view.displayPrev);
            }
        };

        function getDateFromEl(el) {
            return moment( $(el).data(DATA.date) );
        }

        function selectionFrom(date) {
            if (!date) {
                return moment( $datepicker.data(DATA.selectFrom) );
            }

            $datepicker.data(DATA.selectFrom, date.format('YYYY-MM-DD'));
            refreshDatepicker();
        }

        function selectionTo(date) {
            if (!date) {
                return moment( $datepicker.data(DATA.selectTo) );
            }

            $datepicker.data(DATA.selectTo, date.format('YYYY-MM-DD'));
            refreshDatepicker();
        }

        function clearSelection() {
            $datepicker.removeData(DATA.selectFrom);
            $datepicker.removeData(DATA.selectTo);
            refreshDatepicker();
        }

        function sameOrBetween(current, from, to) {
            return current.isSame(from) || current.isSame(to) || ( current.isAfter(from) && current.isBefore(to) );
        }

        function refreshDatepicker() {

            var from = selectionFrom();
            var to   = selectionTo();

            $('.' + CSS.day).each(function() {
                var d = moment( $(this).data(DATA.date) );
                select(this, sameOrBetween(d, from, to));
            });
        }

        function bookHoliday(from, to) {

            function format(d) {
                return d.format('dd, DD. MMMM YY');
            }

            // TODO redirect to new page
            console.log('booking holiday from ' + format(from) + ' to ' + format(to));
        }

        function select(el, select) {

            var $el = $(el);

            if ( ! $el.data(DATA.selectable) ) {
                return;
            }

            if (!!select) {
                $el.addClass(CSS.daySelected);
            }
            else {
                $el.removeClass(CSS.daySelected);
            }
        }

        var Controller = {
            bind: function() {

                $datepicker.on('mousedown', '.' + CSS.day , datepickerHandlers.mousedown);
                $datepicker.on('mouseover', '.' + CSS.day , datepickerHandlers.mouseover);
                $datepicker.on('click'    , '.' + CSS.day , datepickerHandlers.click    );

                $datepicker.on('click'    , '.' + CSS.prev, datepickerHandlers.clickPrev);
                $datepicker.on('click'    , '.' + CSS.next, datepickerHandlers.clickNext);


                $(document.body).on('keyup', function(e) {
                    if (e.keyCode === keyCodes.escape) {
                        clearSelection();
                    }
                });

                $(document.body).on('mouseup', function() {
                    $(document.body).removeClass(CSS.mousedown);
                });
            }
        };

        return {
            create: function(_holidayService, _view) {
                holidayService = _holidayService;
                view = _view;
                return Controller;
            }
        };
    }());


    var Calendar = {
        init: function(holidayService) {

            var a = Assertion.create (holidayService);
            var v = View.create(a);
            var c = Controller.create(holidayService, v);

            v.display();
            c.bind();
        }
    };

    /**
     * @export
     */
    window.Urlaubsverwaltung = {
        Calendar      : Calendar,
        HolidayService: HolidayService
    };

});

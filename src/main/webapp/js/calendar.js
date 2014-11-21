// TODO half holidays
// TODO description of day colors
// TODO selectedDays -> mouseover -> tooltip how many holidays are selected
// TODO highlight selected days in red if remaining holidays are not enough

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
        day                   : 'datepicker-day',
        daySelected           : 'datepicker-day-selected',
        dayToday              : 'datepicker-day-today',
        dayWeekend            : 'datepicker-day-weekend',
        dayPast               : 'datepicker-day-past',
        dayHalf               : 'datepicker-day-half',
        dayPublicHoliday      : 'datepicker-day-public-holiday',
        dayHalfPublicHoliday  : 'datepicker-day-half-public-holiday',
        dayPersonalHoliday    : 'datepicker-day-personal-holiday',
        dayHalfPersonalHoliday: 'datepicker-day-half-personal-holiday',
        next                  : 'datepicker-next',
        prev                  : 'datepicker-prev',
        month                 : 'datepicker-month',
        monthNext             : 'datepicker-month-next',
        monthPrev             : 'datepicker-month-prev',
        mousedown             : 'mousedown'
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
            isHalfDay: function(date) {
              return holidayService.isHalfDay(date);
            },
            title: function(date) {
              return holidayService.getDescription(date);
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

        var webPrefix;
        var apiPrefix;
        var personId;

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
                url: apiPrefix + query,
                dataType: 'json'
            });
        }

        function cacheHoliday(response) {

            var c = _CACHE['holiday'] = _CACHE['holiday'] || {};

            $.each(response, function(idx, data) {
                var date = data.date;
                var y = date.match(/\d{0,4}/)[0];
                c[y] = c[y] || [];
                c[y].push(data);
            });
        }

        function cacheData(type) {
            var c = _CACHE[type] = _CACHE[type] || {};
            return function(data) {
                
                var publicHolidays = data.response.publicHolidays;
                
                $.each(publicHolidays, function(idx, publicHoliday) {
                    var date = publicHoliday.date;
                    var y = date.match(/\d{0,4}/)[0];
                    c[y] = c[y] || [];
                    c[y].push(publicHoliday);
                });
            }
        }

        function isHoliday(type) {
          return function (date) {

            var year = date.year();
            var formattedDate = date.format('YYYY-MM-DD');

            if(_CACHE[type][year]) {

              return _.findWhere(_CACHE[type][year], {date: formattedDate}) !== undefined;

            }

            return false;
          };
        }

        var HolidayService = {

            isPersonalHoliday: isHoliday('holiday'),

            isPublicHoliday: isHoliday('publicHoliday'),

            isHalfDay: function (date) {

              var year = date.year();
              var formattedDate = date.format('YYYY-MM-DD');

              if(_CACHE['publicHoliday'][year]) {

                var publicHoliday = _.findWhere(_CACHE['publicHoliday'][year], {date: formattedDate});

                if(publicHoliday && publicHoliday.dayLength === 0.5) {
                  return true;
                }

              }

              if(_CACHE['holiday'][year]) {

                var personalHoliday = _.findWhere(_CACHE['holiday'][year], {date: formattedDate});

                if(personalHoliday && personalHoliday.dayLength === 0.5) {
                  return true;
                }

              }

              return false;
            },

            getDescription: function (date) {

              var year = date.year();
              var formattedDate = date.format('YYYY-MM-DD');

              if(_CACHE['publicHoliday'][year]) {

                var publicHoliday = _.findWhere(_CACHE['publicHoliday'][year], {date: formattedDate});

                if(publicHoliday) {
                  return publicHoliday.description;
                }

              }

              return '';

            },

            /**
             *
             * @param {moment} from
             * @param {moment} [to]
             */
            bookHoliday: function(from, to) {

                var params = {
                    from :      from.format('YYYY-MM-DD'),
                    to   : to ? to  .format('YYYY-MM-DD') : undefined
                };

                document.location.href = webPrefix + '/application/new' + paramize( params );
            },

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
                    return fetch('/public-holiday', {year: year}).success( cacheData('publicHoliday') );
                }
            },

            /**
             *
             * @param {number} personId
             * @param {number} year
             * @param {number} [month]
             * @returns {$.ajax}
             */
            fetchPersonal: function(year) {
                var deferred = $.Deferred();

                _CACHE['holiday'] = _CACHE['holiday'] || {};

                if (_CACHE['holiday'][year]) {
                    return deferred.resolve( _CACHE[year] );
                }
                else {
                    return fetch('/vacation/application-info', {person: personId, year: year}).success( cacheHoliday );
                }
            }
        };

        return {
            create: function(_webPrefix, _apiPrefix, _personId) {
                webPrefix = _webPrefix;
                apiPrefix = _apiPrefix;
                personId  = _personId;
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

            day: '<span class="datepicker-day {{css}}" title="{{title}}" data-datepicker-date="{{date}}" data-datepicker-selectable="{{selectable}}">{{day}}</span>'
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

        function calculateNumberOfMonths() {

            var minTick = 6;

            var datePickerWidth = $("#datepicker").width();

            var prevNextButtonWidth = 20;
            var prevNextButtonMargin = minTick;
            var prevNextButtonPadding = minTick;
            var prevNextButtonsWidth = (2 * prevNextButtonWidth) + 2 * (prevNextButtonMargin * 2) + 2 * (prevNextButtonPadding * 2);

            var datePickerMonthWidth = 230;
            
            var datePickerMonthsContainerMargin = 2 * minTick;
            var datePickerMonthsContainerBorder = 2 * 1;
            
            var datePickerElementsWidth = prevNextButtonsWidth + datePickerMonthsContainerMargin + datePickerMonthsContainerBorder;

            var placeForDatePickerMonths = datePickerWidth - datePickerElementsWidth;
            
            var numberOfMonths = Math.floor(placeForDatePickerMonths / datePickerMonthWidth);

            if(numberOfMonths === 0) {
                return numberOfMonths;
            }

            var totalWidth = (numberOfMonths * datePickerMonthWidth) + datePickerElementsWidth;
            
            if(totalWidth > datePickerWidth) {
                numberOfMonths = 
                    Math.floor(placeForDatePickerMonths / (datePickerMonthWidth + datePickerElementsWidth)); 
            }

            console.log("Displaying " + (numberOfMonths) + " months");

            return numberOfMonths;

        }

        function renderCalendar() {

            var date = moment();
            // 0 index
            var calculatedNumberOfMonths = calculateNumberOfMonths();

            var monthsToShow = 0;

            if(calculatedNumberOfMonths > 1) {
                monthsToShow = calculatedNumberOfMonths - 1;
            }

            return render(TMPL.container, {

                prevBtn   : renderButton ( CSS.prev, '<i class="fa fa-chevron-left"></i>'),
                nextBtn   : renderButton ( CSS.next, '<i class="fa fa-chevron-right"></i>'),
                prevMonth : renderMonth  ( moment(date).add('M', -1), CSS.monthPrev ),
                nextMonth : renderMonth  ( moment(date).add('M', monthsToShow) , CSS.monthNext ),

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
                    assert.isHalfDay         (date) ? CSS.dayHalf            : ''
                ].join(' ');
            }

            function isSelectable() {

                // NOTE: Order is important here!

                var isPast = assert.isPast(date);
                var isWeekend = assert.isWeekend(date);

                if(isPast || isWeekend) {
                  return false;
                }

                var isHalfDay = assert.isHalfDay(date);

                if(isHalfDay) {
                  return true;
                }

                var isPublicHoliday = assert.isPublicHoliday(date);
                var isPersonalHoliday = assert.isPersonalHoliday(date);

                if(isPublicHoliday || isPersonalHoliday) {
                  return false;
                }

                return true;

            }

            return render(TMPL.day, {
                date: date.format('YYYY-MM-DD'),
                day : date.format('DD'),
                css : classes(),
                selectable: isSelectable(),
                title: assert.title(date)
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

                var isSelectable = $(this).attr("data-datepicker-selectable");

                if (isSelectable === "true" && sameOrBetween(dateThis, dateFrom, dateTo)) {
                    holidayService.bookHoliday(dateFrom, dateTo);
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
                    holidayService.fetchPublic   ( date.year() ),
                    holidayService.fetchPersonal ( date.year() )
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
                    holidayService.fetchPublic   ( date.year() ),
                    holidayService.fetchPersonal ( date.year() )
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
        
        view: null,
        
        init: function(holidayService) {

            var a = Assertion.create (holidayService);
            view = View.create(a);
            var c = Controller.create(holidayService, view);

            view.display();
            c.bind();
        },
        
        reRender: function() {
            view.display();
            
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

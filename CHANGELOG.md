### next release
* Use comment date for application cancel requested state of application [#1089](https://github.com/synyx/urlaubsverwaltung/pull/1089)
* Dependency upgrades see [milestone](https://github.com/synyx/urlaubsverwaltung/milestone/69?closed=1)

### [urlaubsverwaltung-3.4.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.4.0)
* Add application forwards into comments [#1065](https://github.com/synyx/urlaubsverwaltung/pull/1065)
* Dependency upgrades see [milestone](https://github.com/synyx/urlaubsverwaltung/milestone/68?closed=1)

### [urlaubsverwaltung-3.3.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.3.0)
* Enable calendar interaction for past days [#1017](https://github.com/synyx/urlaubsverwaltung/pull/1017)
* Improve height of datepicker months container to show last row [#1054](https://github.com/synyx/urlaubsverwaltung/pull/1054)
* Consider 'de-XX' locales for date-fns translations additionally to 'de' [#1018](https://github.com/synyx/urlaubsverwaltung/pull/1018) 
* Lot of dependency upgrades see [milestone](https://github.com/synyx/urlaubsverwaltung/milestone/67?closed=1)

### [urlaubsverwaltung-3.2.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.2.1)
* Add navigate dependency to overtime overview [#1014](https://github.com/synyx/urlaubsverwaltung/pull/1014)
* Long term caching [#1013](https://github.com/synyx/urlaubsverwaltung/pull/1013)

### [urlaubsverwaltung-3.2.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.2.0)
* Add sicknotes to calendar [#987](https://github.com/synyx/urlaubsverwaltung/pull/987)
* Add vacation days to overtime calender  [#1007](https://github.com/synyx/urlaubsverwaltung/pull/1007)
* Move absences to absence package and some minor cleanups [#977](https://github.com/synyx/urlaubsverwaltung/pull/977)
* Refactor Dockerfile, changing base image, rename jvm configuration en... [#1006](https://github.com/synyx/urlaubsverwaltung/pull/1006)
* Add enforce plugin to check for java 11 and maven 3.3.9 [#1001](https://github.com/synyx/urlaubsverwaltung/pull/1001)
* Minor typo and comma fixes for calendar share feature [#988](https://github.com/synyx/urlaubsverwaltung/pull/988)
* Upgrade terser-webpack-plugin to 2.3.5
* Upgrade owasp dependency check plugin to 5.3.1
* Upgrade editorconfig maven plugin to 0.0.11
* Upgrade pitest maven plugin to 1.5.0
* Upgrade frontend maven plugin to 1.9.1
* Upgrade httpclient to 4.5.12
* Upgrade httpcore to 4.4.13
* Upgrade micrometer to 1.3.5
* Upgrade google api client and services calendar to 1.30.9

### [urlaubsverwaltung-3.1.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.1.2)
* Fix that a multiple day event ends one day to early [#980](https://github.com/synyx/urlaubsverwaltung/pull/980)
* Fix to add au for one day not in sick note period [#976](https://github.com/synyx/urlaubsverwaltung/pull/976)
* Fix to use default form action instead of google handshake in settings [#975](https://github.com/synyx/urlaubsverwaltung/pull/975)
* Fix to allow vacation overview for department head [#974](https://github.com/synyx/urlaubsverwaltung/pull/974)
* Fix timezone selection for exchange calendar [#985](https://github.com/synyx/urlaubsverwaltung/pull/985)
* Reorder the css loading to fix wrong color of past months for sick notes [#981](https://github.com/synyx/urlaubsverwaltung/pull/981)
* Reorder the css loading to fix wrong color of past months for overtime [#982](https://github.com/synyx/urlaubsverwaltung/pull/982)

### [urlaubsverwaltung-3.1.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.1.1)
* Fix hidden company calendar section after interacting with department calendar [#968](https://github.com/synyx/urlaubsverwaltung/pull/968)
* Fix to use Date in VEvent instead of DateTime for multiple day events [#966](https://github.com/synyx/urlaubsverwaltung/pull/966)
* Fix wrong company calendar url generation [#965](https://github.com/synyx/urlaubsverwaltung/pull/965) [#963](https://github.com/synyx/urlaubsverwaltung/issues/963)
* Fix comment from sick note conversion to vacation is missing [#689](https://github.com/synyx/urlaubsverwaltung/issues/689) [#962](https://github.com/synyx/urlaubsverwaltung/pull/962) 
* Fix wrong shared calendar url behind proxy [#970](https://github.com/synyx/urlaubsverwaltung/issues/970) [#973](https://github.com/synyx/urlaubsverwaltung/pull/973) 
* Upgrade docker maven plugin to 0.33.0
* Upgrade spring boot parent to 2.1.13

### [urlaubsverwaltung-3.1.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.1.0)
* Provide ical functionality [#583](https://github.com/synyx/urlaubsverwaltung/pull/583) 
* Remove html5 validation of addition and reduction of overtime [#834](https://github.com/synyx/urlaubsverwaltung/pull/834) [#693](https://github.com/synyx/urlaubsverwaltung/issues/693)
* Add micrometer export for [Stackdriver](https://micrometer.io/docs/registry/stackdriver)
* Frontend asset tooling [#921](https://github.com/synyx/urlaubsverwaltung/pull/921)
* Improve user role edit form [#922](https://github.com/synyx/urlaubsverwaltung/pull/922)
* Fix coverage in builds [#929](https://github.com/synyx/urlaubsverwaltung/pull/929)
* Fix no application for leave information of people in the same department [#949](https://github.com/synyx/urlaubsverwaltung/pull/949)
* Rename overtime validator to overtime form validator [#925](https://github.com/synyx/urlaubsverwaltung/pull/925)
* Vacation overview does not show absences with role boss [#924](https://github.com/synyx/urlaubsverwaltung/issues/924) [#926](https://github.com/synyx/urlaubsverwaltung/pull/926)
* Upgrade jollyday to 0.5.10 [#931](https://github.com/synyx/urlaubsverwaltung/pull/931)
* Upgrade docker maven plugin to 0.32.0 [#932](https://github.com/synyx/urlaubsverwaltung/pull/932)
* Upgrade httpcore to 4.4.12 [#934](https://github.com/synyx/urlaubsverwaltung/pull/934)
* Upgrade google api dependencies [#933](https://github.com/synyx/urlaubsverwaltung/pull/933)
* Remove distribution repository from pom [#936](https://github.com/synyx/urlaubsverwaltung/pull/936)
* Remove sonatype snapshot repository [#937](https://github.com/synyx/urlaubsverwaltung/pull/937)
* Upgrade spring boot parent to 2.1.12

### [urlaubsverwaltung-3.0.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.0.1)
* Fix ldap and ad sync can not be enabled [#914](https://github.com/synyx/urlaubsverwaltung/pull/914)
* Add admin roles message property [#905](https://github.com/synyx/urlaubsverwaltung/pull/905)
* Remove 'reason' from application logging [#901](https://github.com/synyx/urlaubsverwaltung/pull/901)
* Remove 'comment' from account logging [#902](https://github.com/synyx/urlaubsverwaltung/pull/902)
* Remove 'comment' from SickNoteForm logging [#913](https://github.com/synyx/urlaubsverwaltung/pull/913)
* Remove 'reason' and 'comment' from ApplicationForLeaveForm logging [#912](https://github.com/synyx/urlaubsverwaltung/pull/912) 

### [urlaubsverwaltung-3.0.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.0.0)
* Configuration-Change: `uv.person.canBeManipulated` is now `false` by default [#880](https://github.com/synyx/urlaubsverwaltung/pull/880)
* Configuration-Change: Change default security auth mechanism to default [#883](https://github.com/synyx/urlaubsverwaltung/pull/883)
* Fix working time default behaviour [#878](https://github.com/synyx/urlaubsverwaltung/pull/878)
* Fix logging of user data on google calendar event [#875](https://github.com/synyx/urlaubsverwaltung/pull/875)
* Fix to early deleted mail configuration [#877](https://github.com/synyx/urlaubsverwaltung/pull/877)
* Fix exception handling [#899](https://github.com/synyx/urlaubsverwaltung/pull/899)
* Catch `FreeTimeAbsenceException` on availability api if working time not set [#862](https://github.com/synyx/urlaubsverwaltung/pull/862)
* Improve a11y [#871](https://github.com/synyx/urlaubsverwaltung/pull/871) [#868](https://github.com/synyx/urlaubsverwaltung/pull/868) [#896](https://github.com/synyx/urlaubsverwaltung/pull/896) [#896](https://github.com/synyx/urlaubsverwaltung/pull/896) [#879](https://github.com/synyx/urlaubsverwaltung/pull/879) [#864](https://github.com/synyx/urlaubsverwaltung/pull/864) [#865](https://github.com/synyx/urlaubsverwaltung/pull/865)
* Add Mailhog as local smtp service [#884](https://github.com/synyx/urlaubsverwaltung/pull/884)
* Upgrade spring boot parent to 2.1.11
* Upgrade maven wrapper to 3.6.3
* Some cleanups [#897](https://github.com/synyx/urlaubsverwaltung/pull/897) [#889](https://github.com/synyx/urlaubsverwaltung/pull/889) [#882](https://github.com/synyx/urlaubsverwaltung/pull/882) [#866](https://github.com/synyx/urlaubsverwaltung/pull/866)


### [urlaubsverwaltung-3.0.0.RC8](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.0.0.RC8)
* API-Change: Move `api/availability` to `api/person/persons/{personId}/availabilities` [#808](https://github.com/synyx/urlaubsverwaltung/issues/808)
* Add thuringia childrens day holiday [#790](https://github.com/synyx/urlaubsverwaltung/pull/790)
* Add default workdays to the configuration properties [#828](https://github.com/synyx/urlaubsverwaltung/pull/828)
* Add the correct locale to as html element attribute [#856](https://github.com/synyx/urlaubsverwaltung/pull/856)
* Add scope attribute to th to define col or row [#857](https://github.com/synyx/urlaubsverwaltung/pull/857)
* Add favicons for all types of devices [#819](https://github.com/synyx/urlaubsverwaltung/pull/819)
* Allow user to get their own data via the api [#825](https://github.com/synyx/urlaubsverwaltung/pull/825)
* Fix missing absence entries in availabilities api [#829](https://github.com/synyx/urlaubsverwaltung/pull/829)
* Fix broken selector label on persons page [#860](https://github.com/synyx/urlaubsverwaltung/pull/860)
* Fix user can be manipulated [#822](https://github.com/synyx/urlaubsverwaltung/pull/822)
* Rename loginName to username [#839](https://github.com/synyx/urlaubsverwaltung/pull/839)
* Rename to view controller test [#814](https://github.com/synyx/urlaubsverwaltung/pull/814)
* Remove all deprecations [#841](https://github.com/synyx/urlaubsverwaltung/pull/841)
* Upgrade spring boot parent to 2.1.10
* Upgrade google api client to 1.25.0
* Upgrade jollyday to 0.5.9
* Upgrade frontend maven plugin to 1.8.0
* Upgrade maven wrapper to 3.6.2

### [urlaubsverwaltung-3.0.0.RC7](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.0.0.RC7)
* Remove liquibase user demo creation on demo context [#810](https://github.com/synyx/urlaubsverwaltung/pull/810)

### [urlaubsverwaltung-3.0.0.RC6](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.0.0.RC6)
* Introduce application.properties based mail configuration [#634](https://github.com/synyx/urlaubsverwaltung/pull/634)
* Add ability to create new user with default working days and vacation days [#365](https://github.com/synyx/urlaubsverwaltung/issues/365) [#805](https://github.com/synyx/urlaubsverwaltung/pull/805)
* Add possibility to disable new user creation [#807](https://github.com/synyx/urlaubsverwaltung/pull/807)

### [urlaubsverwaltung-3.0.0.RC5](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.0.0.RC5)
* Fix broken link in department overview [#749](https://github.com/synyx/urlaubsverwaltung/pull/749)
* Fix highlight current day on vacation request [#751](https://github.com/synyx/urlaubsverwaltung/pull/751)
* Decouple auth from api [#759](https://github.com/synyx/urlaubsverwaltung/pull/759)
* API-Change: `api/availability` now uses `personId` instead of `loginName`. The id of a person can be discovered via `api/person` [736](https://github.com/synyx/urlaubsverwaltung/issues/736)
* ❗Security-Fix: Activate method security for @Pre and @PostAuthorize [#761](https://github.com/synyx/urlaubsverwaltung/pull/761)
* Secure Api Access [#695](https://github.com/synyx/urlaubsverwaltung/pull/695)
* Upgrade spring boot parent to 2.1.7
* Upgrade NodeJS to 12
* Upgrade date-fns from 1.x to 2.0.1

### [urlaubsverwaltung-3.0.0.RC4](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.0.0.RC4)
* Fix login parameter security

### [urlaubsverwaltung-3.0.0.RC3](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.0.0.RC3)
* Add overtime controller tests [#744](https://github.com/synyx/urlaubsverwaltung/pull/744)
* Improve Readme [#743](https://github.com/synyx/urlaubsverwaltung/pull/743)
* Allow to add username and password via url and add demo system to readme [#742](https://github.com/synyx/urlaubsverwaltung/pull/742)

### [urlaubsverwaltung-3.0.0.RC2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.0.0.RC2)
* Fix docker image build [#740](https://github.com/synyx/urlaubsverwaltung/pull/740)
* Fix favicon security [#741](https://github.com/synyx/urlaubsverwaltung/pull/741)
* Fixes and cleanups [#739](https://github.com/synyx/urlaubsverwaltung/pull/739)

### [urlaubsverwaltung-3.0.0.RC1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-3.0.0.RC1)
* Upgrade to java 11 [#518](https://github.com/synyx/urlaubsverwaltung/pull/518)
* Upgrade spring boot to 2.1.6 [#710](https://github.com/synyx/urlaubsverwaltung/pull/710)
* Migrate database to mariadb [#518](https://github.com/synyx/urlaubsverwaltung/pull/518)
* Migrate double used configuration paths for uv.security [#712](https://github.com/synyx/urlaubsverwaltung/pull/712)
* Add configuration properties for security and some cleanups [#696](https://github.com/synyx/urlaubsverwaltung/pull/696)
* Add new security provider with OpenId Connect (oidc) [#716](https://github.com/synyx/urlaubsverwaltung/pull/716)
* Fix availability and workdays api: Use http status 204 (instead of 500) for no working days present for given user [#728](https://github.com/synyx/urlaubsverwaltung/issues/728)

### [urlaubsverwaltung-2.44.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.44.2)
* ❗Security-Fix: Activate method security for @Pre and @PostAuthorize [#760](https://github.com/synyx/urlaubsverwaltung/issues/760)
* Fix test data creation for application (No application date created)

### [urlaubsverwaltung-2.44.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.44.1)
* Fix availability and workdays api: Use http status 204 (instead of 500) for no working days present for given user [#728](https://github.com/synyx/urlaubsverwaltung/issues/728)

### [urlaubsverwaltung-2.44.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.44.0)
* Added comment in sick-note creation dialog [#472](https://github.com/synyx/urlaubsverwaltung/issues/472)
* Quickfix for better overview about department members: add link to department
  members on departments page [#233](https://github.com/synyx/urlaubsverwaltung/issues/233)
* Fix double half day applications for leave on Christmas/New Year's Eve [#478](https://github.com/synyx/urlaubsverwaltung/issues/478)
* Fix of empty calender in sick note edit form [#708](https://github.com/synyx/urlaubsverwaltung/issues/708)

### [urlaubsverwaltung-2.43.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.43.2)
* Fix release for broken war / container image

### [urlaubsverwaltung-2.43.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.43.0)
* Make event subjects for calendar sync more generic [#198](https://github.com/synyx/urlaubsverwaltung/issues/198) + [#654](https://github.com/synyx/urlaubsverwaltung/issues/654)
* Fix security vulnerabilities [#704](https://github.com/synyx/urlaubsverwaltung/pull/704) [#705](https://github.com/synyx/urlaubsverwaltung/pull/705)

### [urlaubsverwaltung-2.42.3](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.42.3)
* Fix login issues caused by missing ldap security configuration
  properties [#697](https://github.com/synyx/urlaubsverwaltung/pull/697)

### [urlaubsverwaltung-2.42.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.42.2)
* Replace h2 with mysql for development [#694](https://github.com/synyx/urlaubsverwaltung/pull/694)
* Fix calculation of days between start date and end date of sick notes [#686](https://github.com/synyx/urlaubsverwaltung/issues/686)
* Fix (missing) asset caching [#684](https://github.com/synyx/urlaubsverwaltung/issues/684)

### [urlaubsverwaltung-2.42.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.42.1)
* Fix mail notification permissions [#681](https://github.com/synyx/urlaubsverwaltung/pull/681)

### [urlaubsverwaltung-2.42.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.42.0)
* Upgrade jquery to 3.4.1 [#679](https://github.com/synyx/urlaubsverwaltung/pull/679)
* Allow assignment of departments for bosses [#317](https://github.com/synyx/urlaubsverwaltung/issues/317) [#374](https://github.com/synyx/urlaubsverwaltung/issues/374)

### [urlaubsverwaltung-2.41.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.41.0)
* Show morning, noon and full absence in calendar [#624](https://github.com/synyx/urlaubsverwaltung/issues/624)
* Fix second stage auth does not have to be in department [#663](https://github.com/synyx/urlaubsverwaltung/pull/663) [#192](https://github.com/synyx/urlaubsverwaltung/issues/192) [#439](https://github.com/synyx/urlaubsverwaltung/issues/439) [#317](https://github.com/synyx/urlaubsverwaltung/issues/317)
* Set annualVacation and actualVacation settings steps to 0.5 [#664](https://github.com/synyx/urlaubsverwaltung/issues/664)

### [urlaubsverwaltung-2.40.6](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.40.6)
* Calendar settings not configurable for google and exchange [#675](https://github.com/synyx/urlaubsverwaltung/pull/675)

### [urlaubsverwaltung-2.40.5](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.40.5)
* Fix broken umlauts on error page [#665](https://github.com/synyx/urlaubsverwaltung/issues/665)
* Fix creation of admin user for testdata [#662](https://github.com/synyx/urlaubsverwaltung/pull/662)
* Fix overlapping temporary allowed applications [#325](https://github.com/synyx/urlaubsverwaltung/issues/325)
* Show approvable applications for departmentHead and secondStageAuth [#657](https://github.com/synyx/urlaubsverwaltung/pull/657)
* Fix character counter in overtime form [#673](https://github.com/synyx/urlaubsverwaltung/issues/673)
* Fix i18n in dynamic application information [#558](https://github.com/synyx/urlaubsverwaltung/issues/558)

### [urlaubsverwaltung-2.40.4](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.40.4)
* Fix maximum sick pay days [#579](https://github.com/synyx/urlaubsverwaltung/issues/579) [#650](https://github.com/synyx/urlaubsverwaltung/pull/650)
* Allow half day input on account form for remaining vacation days [#652](https://github.com/synyx/urlaubsverwaltung/pull/652)
* Replace … with ... [#656](https://github.com/synyx/urlaubsverwaltung/pull/656)
* Fix workdays and vacations api usage [#651](https://github.com/synyx/urlaubsverwaltung/pull/651)

### [urlaubsverwaltung-2.40.3](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.40.3)
* Fix autocompletion for sick note datepicker [#644](https://github.com/synyx/urlaubsverwaltung/pull/644)
* Fix character count in textareas of application form [#647](https://github.com/synyx/urlaubsverwaltung/issues/647)

### [urlaubsverwaltung-2.40.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.40.2)
* Allow overtime entries in 0.25 hour steps [#638](https://github.com/synyx/urlaubsverwaltung/pull/638)
* Fix jquery ui themes import [#639](https://github.com/synyx/urlaubsverwaltung/pull/639)
* Fix datepicker for sick notes [#641](https://github.com/synyx/urlaubsverwaltung/pull/641)
* Fix datepicker for overtime [#642](https://github.com/synyx/urlaubsverwaltung/pull/642)

### [urlaubsverwaltung-2.40.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.40.1)
* Add missing fmt import in overview [#628](https://github.com/synyx/urlaubsverwaltung/pull/628)
* Fix sicknote to vacation converting [#635](https://github.com/synyx/urlaubsverwaltung/pull/635)
* Fix to use correct java 8 DateTimeFormatter [#631](https://github.com/synyx/urlaubsverwaltung/pull/631)
* Fix to hide unused input fields in E-Mails [#585](https://github.com/synyx/urlaubsverwaltung/issues/585) [#633](https://github.com/synyx/urlaubsverwaltung/pull/630)
* Add toString to VacationDaysLeft [#629](https://github.com/synyx/urlaubsverwaltung/pull/629)

### [urlaubsverwaltung-2.40.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.40.0)
* Improve input fields [#597](https://github.com/synyx/urlaubsverwaltung/pull/597)
* Architecture refactoring [#595](https://github.com/synyx/urlaubsverwaltung/pull/595)
* Add .editorconfig lint [#594](https://github.com/synyx/urlaubsverwaltung/pull/594)
* Code Cleanups [#590](https://github.com/synyx/urlaubsverwaltung/pull/590) [#592](https://github.com/synyx/urlaubsverwaltung/pull/592) [#588](https://github.com/synyx/urlaubsverwaltung/pull/588) [#591](https://github.com/synyx/urlaubsverwaltung/pull/591)
* Improve javadoc [#589](https://github.com/synyx/urlaubsverwaltung/pull/589) [#593](https://github.com/synyx/urlaubsverwaltung/pull/593)
* Optimierung der JavaScript / CSS Assets [#572](https://github.com/synyx/urlaubsverwaltung/pull/572)
* Integration des maven frontend plugin und js/css tooling [#390](https://github.com/synyx/urlaubsverwaltung/issues/390)
* Upgrade bootstrap to 3.4.1 [#603](https://github.com/synyx/urlaubsverwaltung/pull/603)
* No autocomplete on 'Email delivery' Settings username and password [#255](https://github.com/synyx/urlaubsverwaltung/issues/255)
* Exchange calendar delete discovery directory logging [#604](https://github.com/synyx/urlaubsverwaltung/pull/604)
* Replace joda with java time [#613](https://github.com/synyx/urlaubsverwaltung/pull/613)
* Fix NPE when creating a SickNote [#627](https://github.com/synyx/urlaubsverwaltung/pull/627) [#625](https://github.com/synyx/urlaubsverwaltung/issues/625)
* Fix #585 "Unausgefüllte Felder in E-Mails nicht ausgeblendet" [#630](https://github.com/synyx/urlaubsverwaltung/pull/630)

### [urlaubsverwaltung-2.39.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.39.0)
* Bugfix: (Rest)urlaub wird nicht korrekt berechnet [#372](https://github.com/synyx/urlaubsverwaltung/issues/372) [#551](https://github.com/synyx/urlaubsverwaltung/pull/551)
* Verbesserung Urlaubsübersicht durch dynamische Sortierung [#395](https://github.com/synyx/urlaubsverwaltung/issues/395)
* Kleine inhaltliche Verbesserungen in den E-Mail-Templates [#580](https://github.com/synyx/urlaubsverwaltung/issues/580) & [#584](https://github.com/synyx/urlaubsverwaltung/issues/584)
* Fix for double account creation [#457](https://github.com/synyx/urlaubsverwaltung/issues/457)
* Add a separat security config for rest api [#571](https://github.com/synyx/urlaubsverwaltung/pull/571)

### [urlaubsverwaltung-2.38.3](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.38.3)
* Bugfix: Fehlende englische Übersetzung für die Urlaubsübersicht [#559](https://github.com/synyx/urlaubsverwaltung/pull/559)
* Bugfix: Falls der 'server.servlet.context-path' gesetzt wird kann man sich nicht einloggen [#565](https://github.com/synyx/urlaubsverwaltung/pull/565)

### [urlaubsverwaltung-2.38.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.38.2)
* Bugfix: Fehlerhafte Übersetzung in den Email-Templates des Urlaubtypes [#560](https://github.com/synyx/urlaubsverwaltung/pull/560)

### [urlaubsverwaltung-2.38.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.38.1)
* Bugfix: Fehlende englishe Übersetzung für Exchange (EWS) URL ergänzt [#557](https://github.com/synyx/urlaubsverwaltung/pull/557)
* Bugfix: Caching von statischen Resourcen reaktiviert [#556](https://github.com/synyx/urlaubsverwaltung/pull/556)

### [urlaubsverwaltung-2.38.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.38.0)
* Replace request mapping [#544](https://github.com/synyx/urlaubsverwaltung/pull/544)
* Bugfix: DockerHub label `latest` wurde nicht veröffentlicht [#549](https://github.com/synyx/urlaubsverwaltung/pull/549)

### [urlaubsverwaltung-2.37.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.37.1)
* Fix basic auth Zugriff auf die API [#545](https://github.com/synyx/urlaubsverwaltung/pull/545)

### [urlaubsverwaltung-2.37.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.37.0)
* Exchange Kalender Integration: Manuelle Pflege der EWS URL ermöglicht [#524](https://github.com/synyx/urlaubsverwaltung/pull/524)
* Exchange Kalender Integration: Kalender für Synchronisation konfigurierbar gemacht [#527](https://github.com/synyx/urlaubsverwaltung/pull/527)
* Exchange Kalender Integration: Timezone für Exchange-Kalender hinzugefügt [#452](https://github.com/synyx/urlaubsverwaltung/pull/452)
* Englische Übersetzung hinzugefügt [#516](https://github.com/synyx/urlaubsverwaltung/pull/516)
* Neues buntes Favicon hinzugefügt [#536](https://github.com/synyx/urlaubsverwaltung/pull/536)

### [urlaubsverwaltung-2.36.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.36.2)
* Fixup Release: Veröffentlichung der Dockercontainer berichtigt

### [urlaubsverwaltung-2.36.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.36.1)
* Bugfix: Dependency Conflicts

### [urlaubsverwaltung-2.36.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.36.0)
* Upgrade auf Spring Boot 2.1.3 [#501](https://github.com/synyx/urlaubsverwaltung/pull/501) 
* Fix Bug bei dem die Überstunden trotz Deaktivierung angezeigt wurden [#511](https://github.com/synyx/urlaubsverwaltung/pull/511) 
* Fix Bug beim Generieren von Links in Emails [#517](https://github.com/synyx/urlaubsverwaltung/pull/517)
* Upgrade Swagger API Spezifikation von Version 1.2 zu 2 [#523](https://github.com/synyx/urlaubsverwaltung/pull/523)

### [urlaubsverwaltung-2.35.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.35.0)
* Fix mariadb -> mysql connector [#509](https://github.com/synyx/urlaubsverwaltung/pull/509) 

### [urlaubsverwaltung-2.34.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.34.0)
* Bug: Gravatar reaktiviert [#502](https://github.com/synyx/urlaubsverwaltung/pull/502)  
* Neue Feiertag Internationaler Frauentag für das Bundesland Berlin hinzugefügt [#477](https://github.com/synyx/urlaubsverwaltung/pull/477)

### [urlaubsverwaltung-2.33.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.33.0)
* Aktuelle Spring Boot 1.5 Version [#490](https://github.com/synyx/urlaubsverwaltung/pull/490)
* Logging auf SLF4J umgestellt [#489](https://github.com/synyx/urlaubsverwaltung/pull/489)
* Packetierung auf WAR-Dateien umgestellt [#488](https://github.com/synyx/urlaubsverwaltung/pull/488)
* Email-Templating auf Freemaker umgestellt [#487](https://github.com/synyx/urlaubsverwaltung/pull/487)

### [urlaubsverwaltung-2.32.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.32.0)
* Automatisierte Veröffentlichung des Release-JARs in den GitHub Releases

### [urlaubsverwaltung-2.31.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.31.0)
* Automatisierte Veröffentlichung des Release-JARs in den GitHub Releases

### [urlaubsverwaltung-2.30.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.30.0)
* Veröffentlichung der Urlaubsverwaltung auf [Docker Hub](https://hub.docker.com/r/synyx/urlaubsverwaltung/tags) für Releases [#481](https://github.com/synyx/urlaubsverwaltung/pull/481) 

### [urlaubsverwaltung-2.29.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.29.0)
* Bug: help-block überlagert Link für Jahres-Auswahl [#448](https://github.com/synyx/urlaubsverwaltung/issues/448)
* Sicherheitsupdates JS-Abhängigkeiten
* Bereits verbuchter Resturlaub im nächsten Jahr kann in diesem Jahr erneut ausgegeben werden [#447](https://github.com/synyx/urlaubsverwaltung/issues/447)
* Aktualisierung favicon [#459](https://github.com/synyx/urlaubsverwaltung/pull/459)
* Einführung einheitlicher Coding-Guidelines
* Bug: History des Browsers überlappt Kalender [#441](https://github.com/synyx/urlaubsverwaltung/issues/441)
* Verbesserung der Swagger API Dokumentation

### [urlaubsverwaltung-2.28.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.28.0)
* Neue Feiertag Reformationstag für die Bundesländer Schleswig-Holstein, Hamburg, Bremen und Niedersachen hinzugefügt [#445](https://github.com/synyx/urlaubsverwaltung/pull/445) [#438](https://github.com/synyx/urlaubsverwaltung/issues/438) [#416](https://github.com/synyx/urlaubsverwaltung/issues/416)
* Update der Frontend-Test Abhängigkeiten [#446](https://github.com/synyx/urlaubsverwaltung/pull/446)
* Verbesserung der Entwicklerdokumentation (Lokales LDAP) [#433](https://github.com/synyx/urlaubsverwaltung/pull/433)

### [urlaubsverwaltung-2.27.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.27.0)
* Bug: Spring Boot Actuators ohne Authentifizierung aufrufbar [#430](https://github.com/synyx/urlaubsverwaltung/issues/430)
* Sicherheitslücken in Tomcat [#428](https://github.com/synyx/urlaubsverwaltung/issues/428)
* CSV Export für Urlaubsstatistik hinzugefügt 
* Bug: Fix JavaScript-Probleme in der Urlaubsübersicht bei älteren Internet Explorer Versionen [#369](https://github.com/synyx/urlaubsverwaltung/issues/369)
* LDAP-Synchronisationszeitpunkt kann konfigiert werden [#354](https://github.com/synyx/urlaubsverwaltung/pull/354)

### [urlaubsverwaltung-2.26.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.26.2)
* Exchange Kalender: Ganztägige Abwesenheiten werden auch als solche angezeigt
* Google Kalendar: Kalendereinträg enthält auch die betreffende Person als Teilnehmer
* Aktualisierung der Javascript Abhängigkeiten
* Korrektur der Kalenderanzeige für Feiertage die auf einen Sonntag fallen

### [urlaubsverwaltung-2.26.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.26.1)
* Bug: Google Calendar Synchronisation funktioniert nur mit localhost [#377](https://github.com/synyx/urlaubsverwaltung/pull/377) 

### [urlaubsverwaltung-2.26.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.26.0)
* Anbindung an Google Calendar [#8](https://github.com/synyx/urlaubsverwaltung/issues/8)
* Bug: Urlaubsantrag für ganztägig und morgens/mittags an einem Tag möglich [#257](https://github.com/synyx/urlaubsverwaltung/issues/257)
* Kleinere Refactorings (Entfernen von Unterschriftssystem)

### [urlaubsverwaltung-2.25.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.25.0)
* Übersicht über alle Abwesenheiten von [@ajanus](https://github.com/ajanus) hinzu [#350](https://github.com/synyx/urlaubsverwaltung/pull/350)

### [urlaubsverwaltung-2.24.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.24.1)
* Bug: Fix H2 Konfiguration für Entwicklungsumgebung

### [urlaubsverwaltung-2.24.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.24.0)
* Erweiterung der Benachrichtigung bei vorläufiger Genehmigung von Urlaubsanträgen: Hier werden jetzt auch mehrere Abteilungen beachtet.
* Kleiner Refactorings (Paketstruktur und Sonar Issues)

### [urlaubsverwaltung-2.23.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.23.0)
* Kommentar zu Urlaubsanspruch in Urlaubsverwaltung pflegen [#238](https://github.com/synyx/urlaubsverwaltung/issues/238)
* Probleme beim Einrichten einer neuen Installation (Schemamigration) [#264](https://github.com/synyx/urlaubsverwaltung/issues/264)
* Einmaligen Feiertag: Reformationstag [#265](https://github.com/synyx/urlaubsverwaltung/issues/265)
* Depencency Updates: Spring Boot 1.4.2 [#301](https://github.com/synyx/urlaubsverwaltung/pull/301) and Swagger 1.0.2 [#277](https://github.com/synyx/urlaubsverwaltung/issues/277)
* Benutzer-Liste wird nicht vollständig angezeigt [#256](https://github.com/synyx/urlaubsverwaltung/issues/256)

### [urlaubsverwaltung-2.22.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.22.0)
* Verbessertes Logging für die Erinnerungsfunktion bei lange wartenden Urlaubsanträgen
* Verbesserte Beschreibung der Office-Rolle
* Update test-emailaddresses to reduce chance to send spam [#253](https://github.com/synyx/urlaubsverwaltung/issues/253)
* Betreff in E-Mail bei neu beantragtem Urlaub sollte Namen enthalten [#249](https://github.com/synyx/urlaubsverwaltung/issues/249)
* Erweiterung der REST-API um die Schnittstelle [`/availabilities`](http://urlaubsverwaltung-demo.synyx.de/api/index.html#!/availabilities) [#208](https://github.com/synyx/urlaubsverwaltung/issues/208)

### [urlaubsverwaltung-2.21.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.21.1)
* Added additional exchange connection configuration (without domain) [#241](https://github.com/synyx/urlaubsverwaltung/issues/241) 
* crash on start urlaubsverwaltung-2.21.0.jar [#239](https://github.com/synyx/urlaubsverwaltung/issues/239) 
* Regelmäßige Erinnerungsmail bei wartenden Anträgen Einstellungen [#227](https://github.com/synyx/urlaubsverwaltung/issues/227)
* Temporär genehmigte Urlaubsanträge stornieren Abteilungen [#229](https://github.com/synyx/urlaubsverwaltung/issues/229) 
* Berechtigungsanzeige: Abteilungen werden nicht angezeigt [#234](https://github.com/synyx/urlaubsverwaltung/issues/234)

### [urlaubsverwaltung-2.21.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.21.0)
* Fehler beim Anpassen der Benutzerberichtigungen [#226](https://github.com/synyx/urlaubsverwaltung/issues/226) 
* Regelmäßige Erinnerungsmail bei wartenden Anträgen Einstellungen [#227](https://github.com/synyx/urlaubsverwaltung/issues/227) 
* Antrag von Abteilungsleiter nur durch Chef bewilligen [#228](https://github.com/synyx/urlaubsverwaltung/issues/228)
* Anrede mit Vor- und Nachname bei Chef-Mails [#225](https://github.com/synyx/urlaubsverwaltung/issues/225) 

### [urlaubsverwaltung-2.20.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.20.1)
* Bundeslandanzeige bezieht sich auf den Arbeitsort nicht Wohnort [#222](https://github.com/synyx/urlaubsverwaltung/issues/222)

### [urlaubsverwaltung-2.20.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.20.0)
* Benutzerformular: UX verbessern [#216](https://github.com/synyx/urlaubsverwaltung/issues/216)

### [urlaubsverwaltung-2.19.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.19.0)
#### Bug Fix / Konfigurationsänderung

* Problembehebung LDAP/AD Authentifizierung/Sync: Update von Spring Boot Version und Spring LDAP Core [#215](https://github.com/synyx/urlaubsverwaltung/issues/215)

### [urlaubsverwaltung-2.18.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.18.2)
#### Bug Fixes
* Bug: Error beim klicken von "Benutzer anlegen" [#213](https://github.com/synyx/urlaubsverwaltung/issues/213)
* Bug: Urlaub genehmigen in Übersicht "offene Urlaubsanträge" bei zweistufigem Genehmigungsprozess [#212](https://github.com/synyx/urlaubsverwaltung/issues/212) 
* Bug: Urlaub ablehnen in Übersicht "offene Urlaubsanträge" [#209](https://github.com/synyx/urlaubsverwaltung/issues/209) 
* Bug: Editieren von Benutzer fehlende Validierung für invaliden Urlaubsanspruch [#204](https://github.com/synyx/urlaubsverwaltung/issues/204) 

#### Change Request
* Noch nicht genehmigten Urlaub im Kalendar farblich hervorheben [#200](https://github.com/synyx/urlaubsverwaltung/issues/200) 


### [ urlaubsverwaltung-2.18.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.18.1)
####Bug Fixes

*  Korrekte Auswahl des Krankmedlungstyps beim Editieren von Krankmeldungen [#201](https://github.com/synyx/urlaubsverwaltung/issues/201)   

### [urlaubsverwaltung-2.18.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.18.0)
#####Features

*  Bundeslandauswahl pro User ermöglichen [#178](https://github.com/synyx/urlaubsverwaltung/issues/178) 
*  Benutzerliste filterbar nach Abteilung [#136](https://github.com/synyx/urlaubsverwaltung/issues/136) 

#### Bug Fixes

*  deaktivierter User loggt sich ein - Problem im Browser [#190](https://github.com/synyx/urlaubsverwaltung/issues/190) 

### [urlaubsverwaltung-2.17.3](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.17.3)
#### Bug Fixes

*  Benutzer deaktivieren nicht möglich [#188](https://github.com/synyx/urlaubsverwaltung/issues/188)

### [urlaubsverwaltung-2.17.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.17.2)
#### Bug Fixes

* Anzeige der Anträge von Mitarbeitern fehlerhaft (moment is not defined) [#176](https://github.com/synyx/urlaubsverwaltung/issues/176) 
* Einstellung: Überstundenverwaltung deaktivieren wird nicht dauerhaft gespeichert [#183](https://github.com/synyx/urlaubsverwaltung/issues/183) 
* Komma-Zahlen eintragen mit englischer Browser Locale [#186](https://github.com/synyx/urlaubsverwaltung/issues/186)

### [urlaubsverwaltung-2.17.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.17.1)
Mini Fix in Personenformular: Label fixen

### [urlaubsverwaltung-2.17.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.17.0)
#### Bug Fixes

* Bug: Umwandeln von Krankheitstagen in Urlaub funktioniert nicht [#170](https://github.com/synyx/urlaubsverwaltung/issues/170)
* Bug: Fehlerseite bei ungültigem Zeitraum einer Krankmeldung mit AU-Bescheinigung [#164](https://github.com/synyx/urlaubsverwaltung/issues/164)

#### Features

* Urlaubsantrag: Anzeige von Arbeitszeiten [#169](https://github.com/synyx/urlaubsverwaltung/issues/169)
* Urlaubsantrag: Anzeige von Wochentagen [#167](https://github.com/synyx/urlaubsverwaltung/issues/167)
* Benutzerpflege: Vereinfachung der Pflege von Urlaubsanspruch Benutzerpflege [#168](https://github.com/synyx/urlaubsverwaltung/issues/168)
* Benutzerpflege: Validierung bei Vergabe von Berechtigungen verbessern [#163](https://github.com/synyx/urlaubsverwaltung/issues/163)
* Urlaubsantrag: Überstundenanzahl optional bei deaktivierter Überstundenfunktion Einstellungen [#161](https://github.com/synyx/urlaubsverwaltung/issues/161)

### [urlaubsverwaltung-2.16.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.16.0)
#### [Milestone 2.16.0](https://github.com/synyx/urlaubsverwaltung/issues?q=milestone%3Aurlaubsverwaltung-2.16.0+is%3Aclosed)
* Konfiguration: Standardmäßig Cache aktiv und JSP Servlet Development Mode inaktiv
* Feature: Einstellungen E-Mail-Versand erweitern um URL der Anwendung

### [urlaubsverwaltung-2.15.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.15.0)
#### [Milestone 2.15.0](https://github.com/synyx/urlaubsverwaltung/issues?q=milestone%3Aurlaubsverwaltung-2.15.0+is%3Aclosed)
* Feature: Zweistufiger Genehmigungsprozess für Urlaubsanträge [#148](https://github.com/synyx/urlaubsverwaltung/issues/148)
* Feature: E-Mail-Benachrichtung bei neuen Überstundeneinträgen [#147](https://github.com/synyx/urlaubsverwaltung/issues/147)
* Feature: Validierung für maximal mögliche Minusstunden [#146](https://github.com/synyx/urlaubsverwaltung/issues/146)
* Feature: Urlaubsantrag erweitern um Uhrzeit [#145](https://github.com/synyx/urlaubsverwaltung/issues/145) 
* Feature: Urlaubsarten pflegbar machen (Datenbank only) [#144](https://github.com/synyx/urlaubsverwaltung/issues/144)
* Feature: Krankmeldungsarten pflegbar machen (Datenbank only) [#143](https://github.com/synyx/urlaubsverwaltung/issues/143)

### [urlaubsverwaltung-2.14.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.14.1)
* Bug Fix: Es ist nicht möglich, halbtägigen Urlaub zu beantragen [#156](https://github.com/synyx/urlaubsverwaltung/issues/156)

### [urlaubsverwaltung-2.14.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.14.0)
* Enhancement: LDAP/AD Sync optional machen [#142](https://github.com/synyx/urlaubsverwaltung/issues/142)
* Enhancement: Als Mitarbeiter nicht genommenen genehmigten Urlaub stornieren können #11
* Enhancement: Update auf Spring Boot 1.3, Spring Security 4 [#126](https://github.com/synyx/urlaubsverwaltung/issues/126)

### [urlaubsverwaltung-2.13.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.13.2)
* Bug Fix: Krankheitsübersicht nicht möglich, wenn ein Mitarbeiter keine Arbeitszeiten konfiguriert hat [#129](https://github.com/synyx/urlaubsverwaltung/issues/129)

### [urlaubsverwaltung-2.13.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.13.1)
* Bug Fix: (Teilweise) Doppelte Urlaubskontos nach Cronjob zum Jahresanfang [#137](https://github.com/synyx/urlaubsverwaltung/issues/137)
* Bug Fix: Validierung von deaktivierter Exchange Konfiguration [#135](https://github.com/synyx/urlaubsverwaltung/issues/135)

### [urlaubsverwaltung-2.13.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.13.0)
* Maximale Überstunden konfigurieren / Überstundenfunktion implizit deaktivieren [#133](https://github.com/synyx/urlaubsverwaltung/issues/133)
* Zeitraum für Urlaubsstatistik und Krankheitsübersicht kann nun tagesgenau ausgewählt werden [#124](https://github.com/synyx/urlaubsverwaltung/issues/124)

### [urlaubsverwaltung-2.12.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.12.2)
* Exchange Anbindung erfolgt nun über E-Mail-Adresse statt Domäne und Benutzername
* Besseres Logging für Exchange Anbindung für bessere Fehlerverfolgbarkeit

### [urlaubsverwaltung-2.12.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.12.1)
* Bug Fix: Exchange 2013 Kalender Anbindung [#117](https://github.com/synyx/urlaubsverwaltung/issues/117)

### [urlaubsverwaltung-2.12.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.12.0)
* Technisches Feature: Umbau der Urlaubsverwaltung zu einer Spring Boot Anwendung. Ab dieser Version ist die Anwendung eine [Spring Boot](http://projects.spring.io/spring-boot/) Anwendung, d.h. sie wird nicht mehr als WAR in einem Tomcat installiert, sondern als JAR ausgeführt.

### [urlaubsverwaltung-2.11.4](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.11.4)
* Bug Fix: (Teilweise) Doppelte Urlaubskontos nach Cronjob zum Jahresanfang [#137](https://github.com/synyx/urlaubsverwaltung/issues/137)

### [urlaubsverwaltung-2.11.3](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.11.3)
* Bug Fix: Exchange 2013 Kalender Anbindung [#117](https://github.com/synyx/urlaubsverwaltung/issues/117)

### [urlaubsverwaltung-2.11.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.11.2)
* Bug Fix: Überstundenanzahl in Urlaubsstatistik und Überstundenliste wird auf eine Kommastelle aufgerundet
* Genauere Beschreibung siehe in [Milestone Tickets](https://github.com/synyx/urlaubsverwaltung/issues?q=milestone%3Aurlaubsverwaltung-2.11.2+is%3Aclosed)

### [urlaubsverwaltung-2.11.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.11.1)
* Bug Fix: Überstundeneintrag wird aufgerundet
* Genauere Beschreibung siehe in [Milestone Tickets](https://github.com/synyx/urlaubsverwaltung/issues?q=milestone%3Aurlaubsverwaltung-2.11.1+is%3Aclosed)

### [urlaubsverwaltung-2.11.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.11.0)
* Feature: Import/Sync der Benutzerstammdaten aus LDAP/AD bei Anwendungsstart und nächtlich
* Feature: Möglichkeit die LDAP/AD Authentifizierung nur für bestimmte Gruppe zuzulassen
* Feature: Urlaubsstatistik detailliert angezeigt nach Urlaubskategorie
* Feature: Eintragen von Überstunden ermöglichen
* Feature: Urlaub zum Überstundenabbau verknüpfen mit eingetragenen Überstunden
* Bug Fix: Kaputter "Abbrechen" Button im Personenformular
* Genauere Beschreibung siehe in [Tickets](https://github.com/synyx/urlaubsverwaltung/issues?q=milestone%3Aurlaubsverwaltung-2.11.0+is%3Aclosed)

### [urlaubsverwaltung-2.10.5](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.10.5)
* Bug Fix: (Teilweise) Doppelte Urlaubskontos nach Cronjob zum Jahresanfang [#137](https://github.com/synyx/urlaubsverwaltung/issues/137)

### [urlaubsverwaltung-2.10.4](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.10.4)
* Exchange Anbindung über E-Mail-Adresse statt Domäne und Benutzername

### [urlaubsverwaltung-2.10.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.10.2)
* Bug Fix: Exchange 2013 Kalender Anbindung [#117](https://github.com/synyx/urlaubsverwaltung/issues/117)

### [urlaubsverwaltung-2.10.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.10.1)
* Bug: Fix für kaputte Icons und Benutzer-Avatar im Offline-Modus

### [urlaubsverwaltung-2.10.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.10.0)
* Bug Fix: Klick auf Urlaub/Krankmeldung im Übersichtskalender liefert 404
* Feature: Eintragen von halben Krankheitstagen

### [urlaubsverwaltung-2.9.3](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.9.3)
* Bug Fix: Nullpointer in Krankmeldungsübersicht verhindern für inaktive Personen mit Krankmeldungen
* UX: Übersichtskalender Farbschema optimiert und Animation hinzugefügt

### [urlaubsverwaltung-2.9.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.9.2)
* Feature: Beim Einloggen werden Vorname, Nachname und E-Mail-Adresse aus LDAP/AD übernommen
* Feature: Die Einstellungen wurden erweitert um E-Mail-Versand-Konfiguration und Exchange-Kalender-Konfiguration, sodass dies nicht mehr in Property Files gepflegt werden muss.
* Bug Fix: Bei Authentifizierung mit AD kann man sich nun sowohl mit dem Benutzernamen als auch mit der E-Mail einloggen, ohne dass unterschiedliche Benutzer dafür angelegt werden.
* Bug Fix: Für die Exchange Kalender Anbindung kann man nun auch die Domain und entweder E-Mail-Adresse oder Benutzername angeben.

### [urlaubsverwaltung-2.9.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.9.1)
* UX: Wenn ungültiger Zeitraum beim Urlaubsantrag gewählt wird, wird eine Fehlermeldung statt "NaN Tage" als Dauer angezeigt
* UX: Die Mitarbeiterliste kann nun nach Vorname/Nachname gefiltert werden
* Feature: Im Übersichtskalender werden nun auch Krankmeldungen (in rot) angezeigt
* Feature: Im Übersichtskalender werden nun auch noch nicht genehmigte Urlaubsanträge angezeigt. Diese haben die gleiche Farbe wie die genehmigten Urlaubsanträge, um zu vermeiden, dass der Kalender zu bunt wird (Unterscheidung in Feiertag, Urlaub und Krankmeldungen)

### [urlaubsverwaltung-2.9.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.9.0)
* Feature: Anlegen und Bearbeiten von Abteilungen
* Feature: Mitarbeiter zu vorhandenen Abteilungen zuordnen
* Feature: Mitarbeiter zu Abteilungsleitern ernennen. Abteilungsleiter haben die selben Rechte wie Benutzer mit der Rolle Chef - allerdings nur für die Benutzer der Abteilungen, für die sie Abteilungsleiter sind.
* Feature: Beim Beantragen von Urlaub anzeigen, wer aus der eigenen Abteilung zu dem Zeitraum ebenfalls Urlaub hat.
* Feature: Beim Genehmigen/Ablehnen von Urlaub anzeigen, wer aus der Abteilung der Person zu dem Zeitraum ebenfalls Urlaub hat.
* Feature: Die Urlaubsverwaltung kann an einen Exchange Kalender angebunden werden. Dann werden automatisch Termine angelegt, wenn Urlaub beantragt/genehmigt bzw. Krankmeldungen angelegt werden.
* Feature: Benutzer kann auf einer Extraseite seine Daten (Stammdaten, Rollen, Abteilungen, Arbeitszeiten, Urlaubsanspruch) sehen
* UX: Pflichtfelder sind nun mit '\*' markiert
* UX: Formulare zum Antrag stellen, Personen bearbeiten und Einstellungen pflegen wurden überarbeitet und mit Hilfetexten versehen
* UX: Wenn man Urlaub zu weit in der Zukunft beantragt, sieht man bei der Fehlermeldung nun, wie weit man im Voraus Urlaub beantragen darf.

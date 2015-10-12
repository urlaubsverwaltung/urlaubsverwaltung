package org.synyx.urlaubsverwaltung.web.overtime;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * Manage overtime of persons.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class OvertimeController {

    @RequestMapping(value = "/overtime/new", method = RequestMethod.GET)
    public String addOvertime() {

        return "overtime/overtime_form";
    }
}

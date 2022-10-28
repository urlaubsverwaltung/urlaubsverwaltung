package org.synyx.urlaubsverwaltung.department.web;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;
import java.util.regex.Pattern;

import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

/**
 * Validates the content of {@link DepartmentForm}s.
 */
@Component
public class DepartmentViewValidator implements Validator {

    private static final int MAX_CHARS_NAME = 50;
    private static final int MAX_CHARS_DESCRIPTION = 200;

    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_DESCRIPTION = "description";
    private static final String ATTRIBUTE_DEPARTMENT_HEADS = "departmentHeads";
    private static final String ATTRIBUTE_SECOND_STAGE_AUTHORITIES = "secondStageAuthorities";
    private static final String ATTRIBUTE_TWO_STAGE_APPROVAL = "twoStageApproval";

    private static final String ERROR_REASON = "error.entry.mandatory";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";
    private static final String ERROR_DELIMITER = "error.entry.delimiterFound";
    private static final String ERROR_DEPARTMENT_HEAD_NOT_ASSIGNED = "department.members.error.departmentHeadNotAssigned";
    private static final String ERROR_DEPARTMENT_HEAD_NO_ACCESS = "department.members.error.departmentHeadHasNoAccess";

    private static final String ERROR_SECOND_STAGE_AUTHORITY_MISSING = "department.members.error.secondStageAuthorityMissing";
    private static final String ERROR_TWO_STAGE_APPROVAL_FLAG_MISSING = "department.members.error.twoStageApprovalFlagMissing";
    private static final String ERROR_SECOND_STAGE_AUTHORITY_NO_ACCESS = "department.members.error.secondStageHasNoAccess";

    @Override
    public boolean supports(Class<?> clazz) {
        return DepartmentForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        final DepartmentForm departmentForm = (DepartmentForm) target;

        validateName(errors, departmentForm.getName());
        validateDescription(errors, departmentForm.getDescription());
        validateDepartmentHeads(errors, departmentForm.getMembers(), departmentForm.getDepartmentHeads());
        validateSecondStageAuthorities(errors, departmentForm.isTwoStageApproval(), departmentForm.getSecondStageAuthorities());
    }

    private void validateName(Errors errors, String name) {

        final boolean hasText = StringUtils.hasText(name);

        if (!hasText) {
            errors.rejectValue(ATTRIBUTE_NAME, ERROR_REASON);
        }

        if (hasText && name.length() > MAX_CHARS_NAME) {
            errors.rejectValue(ATTRIBUTE_NAME, ERROR_LENGTH);
        }

        final Pattern regex = Pattern.compile(":::");
        if (hasText && regex.matcher(name).find()) {
            errors.rejectValue(ATTRIBUTE_NAME, ERROR_DELIMITER);
        }
    }

    private void validateDescription(Errors errors, String description) {

        final boolean hasText = StringUtils.hasText(description);

        if (hasText && description.length() > MAX_CHARS_DESCRIPTION) {
            errors.rejectValue(ATTRIBUTE_DESCRIPTION, ERROR_LENGTH);
        }
    }

    private void validateDepartmentHeads(Errors errors, List<Person> members, List<Person> departmentHeads) {

        if (departmentHeads != null) {
            for (Person departmentHead : departmentHeads) {
                if (members == null || !members.contains(departmentHead)) {
                    errors.rejectValue(ATTRIBUTE_DEPARTMENT_HEADS, ERROR_DEPARTMENT_HEAD_NOT_ASSIGNED);
                }

                if (!departmentHead.hasRole(Role.DEPARTMENT_HEAD)) {
                    errors.rejectValue(ATTRIBUTE_DEPARTMENT_HEADS, ERROR_DEPARTMENT_HEAD_NO_ACCESS);
                }
            }
        }
    }

    private void validateSecondStageAuthorities(Errors errors, boolean twoStageApproval, List<Person> secondStageAuthorities) {

        if (twoStageApproval) {
            validateSecondStageAuthoritiesHaveRequiredRole(secondStageAuthorities, errors);
        } else {
            validateNoSecondStageAuthoritiesSet(secondStageAuthorities, errors);
        }
    }

    private void validateNoSecondStageAuthoritiesSet(List<Person> secondStageAuthorities, Errors errors) {

        // there must not be any second stage authority
        if (secondStageAuthorities != null && !secondStageAuthorities.isEmpty()) {
            errors.rejectValue(ATTRIBUTE_TWO_STAGE_APPROVAL, ERROR_TWO_STAGE_APPROVAL_FLAG_MISSING);
        }
    }

    private void validateSecondStageAuthoritiesHaveRequiredRole(List<Person> secondStageAuthorities, Errors errors) {

        if (secondStageAuthorities != null && !secondStageAuthorities.isEmpty()) {
            for (Person secondStage : secondStageAuthorities) {

                // second stage authority must have required role
                if (!secondStage.hasRole(SECOND_STAGE_AUTHORITY)) {
                    errors.rejectValue(ATTRIBUTE_SECOND_STAGE_AUTHORITIES, ERROR_SECOND_STAGE_AUTHORITY_NO_ACCESS);
                }
            }
        } else {
            errors.rejectValue(ATTRIBUTE_SECOND_STAGE_AUTHORITIES, ERROR_SECOND_STAGE_AUTHORITY_MISSING);
        }
    }
}

package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewPersonRowCellDto {

    private final int colspan;
    private final String type;
    private final boolean workday;
    private final boolean roundedLeft;
    private final boolean roundedRight;
    private final boolean showText;

    public AbsenceOverviewPersonRowCellDto(int colspan, String type, boolean workday, boolean roundedLeft, boolean roundedRight, boolean showText) {
        this.colspan = colspan;
        this.type = type;
        this.workday = workday;
        this.roundedLeft = roundedLeft;
        this.roundedRight = roundedRight;
        this.showText = showText;
    }

    public int getColspan() {
        return colspan;
    }

    public String getType() {
        return type;
    }

    public boolean isWorkday() {
        return workday;
    }

    public boolean isRoundedLeft() {
        return roundedLeft;
    }

    public boolean isRoundedRight() {
        return roundedRight;
    }

    public boolean isShowText() {
        return showText;
    }
}

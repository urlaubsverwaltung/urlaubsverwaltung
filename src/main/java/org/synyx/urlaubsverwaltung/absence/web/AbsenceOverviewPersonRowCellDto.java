package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewPersonRowCellDto {

    private final int colspan;
    private final String type;
    private final boolean roundedLeft;
    private final boolean roundedRight;
    private final boolean showText;

    public AbsenceOverviewPersonRowCellDto() {
        this(1, "");
    }

    public AbsenceOverviewPersonRowCellDto(int colspan, String type) {
        this(colspan, type, false, false, false);
    }

    public AbsenceOverviewPersonRowCellDto(int colspan, String type, boolean roundedLeft, boolean roundedRight, boolean showText) {
        this.colspan = colspan;
        this.type = type;
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

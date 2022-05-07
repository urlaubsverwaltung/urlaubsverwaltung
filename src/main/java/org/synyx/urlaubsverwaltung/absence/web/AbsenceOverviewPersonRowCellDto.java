package org.synyx.urlaubsverwaltung.absence.web;

import java.util.ArrayList;
import java.util.List;

public class AbsenceOverviewPersonRowCellDto {

    private final int colspan;
    private final String type;
    private final boolean roundedLeft;
    private final boolean roundedRight;
    private final boolean showText;
    private List<Integer> publicHolidayCols;

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
        this.publicHolidayCols = new ArrayList<>();
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

    public List<Integer> getPublicHolidayCols() {
        return publicHolidayCols;
    }

    public void setPublicHolidayCols(List<Integer> publicHolidayCols) {
        this.publicHolidayCols = publicHolidayCols;
    }
}

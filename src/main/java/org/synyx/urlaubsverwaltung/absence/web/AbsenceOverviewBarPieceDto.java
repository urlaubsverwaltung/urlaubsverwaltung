package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewBarPieceDto {

    private final AbsenceBars.Half half;
    private final AbsenceBars.Kind kind;
    private final boolean roundedStart;
    private final boolean roundedEnd;
    private final AbsenceBars.Status status;
    private final String color;
    private final String label;
    private final int labelHalves;
    private final String title;

    @SuppressWarnings("java:S107") // a plain value holder for the template
    AbsenceOverviewBarPieceDto(AbsenceBars.Half half, AbsenceBars.Kind kind, boolean roundedStart, boolean roundedEnd,
                               AbsenceBars.Status status, String color, String label, int labelHalves, String title) {
        this.half = half;
        this.kind = kind;
        this.roundedStart = roundedStart;
        this.roundedEnd = roundedEnd;
        this.status = status;
        this.color = color;
        this.label = label;
        this.labelHalves = labelHalves;
        this.title = title;
    }

    public AbsenceBars.Half getHalf() {
        return half;
    }

    public AbsenceBars.Kind getKind() {
        return kind;
    }

    public boolean isRoundedStart() {
        return roundedStart;
    }

    public boolean isRoundedEnd() {
        return roundedEnd;
    }

    public AbsenceBars.Status getStatus() {
        return status;
    }

    public String getColor() {
        return color;
    }

    public String getLabel() {
        return label;
    }

    public int getLabelHalves() {
        return labelHalves;
    }

    public String getTitle() {
        return title;
    }
}

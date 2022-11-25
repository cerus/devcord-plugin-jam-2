package dev.cerus.pluginjam.unit;

public enum Unit {
    KILOGRAMS("Kilogramm", 1),
    PETER_ALTMAIER("Peter Altmaier", 140),
    GUNNAR_LINDEMANN("Gunnar Lindemann", 120);

    private final String displayName;
    private final int kilograms;

    Unit(final String displayName, final int kilograms) {
        this.displayName = displayName;
        this.kilograms = kilograms;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getKilograms() {
        return this.kilograms;
    }
}

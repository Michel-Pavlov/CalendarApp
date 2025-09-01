package be.hepl.calendarapp.model;

public class Rule implements Comparable<Rule> {
    public Metric metric;
    public ComparatorOp op;
    public double a;
    public double b;
    public String colorHex;
    public int priority = 100;
    public boolean stopOnMatch = true;

    public Rule() {}

    public Rule(Metric metric, ComparatorOp op, double a, double b, String colorHex, int priority) {
        this.metric = metric;
        this.op = op;
        this.a = a;
        this.b = b;
        this.colorHex = colorHex;
        this.priority = priority;
    }

    public boolean matches(double value) {
        return switch (op) {
            case GT -> value > a;
            case GTE -> value >= a;
            case LT -> value < a;
            case LTE -> value <= a;
            case EQ -> Math.abs(value - a) < 1e-9;
            case BETWEEN -> value >= Math.min(a, b) && value <= Math.max(a, b);
        };
    }

    @Override
    public int compareTo(Rule o) {
        return Integer.compare(this.priority, o.priority);
    }
}

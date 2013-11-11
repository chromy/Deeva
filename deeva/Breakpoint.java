package deeva;

public class Breakpoint {
    private String clas;
    private int lineNumber;

    public Breakpoint(String clas, int lineNumber) {
        this.clas = clas;
        this.lineNumber = lineNumber;
    }

    public String getClas() {
        return clas;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Breakpoint)) {
            return false;
        }
        Breakpoint bkpt = (Breakpoint) o;
        return bkpt.getClas().equals(getClas())
            && bkpt.getLineNumber() == getLineNumber();
    }

    @Override
    public int hashCode() {
        return getClas().hashCode() * getLineNumber();
    }
}

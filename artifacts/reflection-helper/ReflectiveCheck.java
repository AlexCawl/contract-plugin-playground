import com.alexcawl.contract.playground.component.v1.ReflectiveEqualsHashCode;

public class ReflectiveCheck {
    static final class NullableSample extends ReflectiveEqualsHashCode {
        private final Object value;

        NullableSample(Object value) {
            this.value = value;
        }
    }

    static final class FloatSample extends ReflectiveEqualsHashCode {
        private final float value;

        FloatSample(float value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        var firstNull = new NullableSample(null);
        var secondNull = new NullableSample(null);
        if (firstNull.equals(secondNull)) throw new AssertionError("Expected null-field defect");
        System.out.println("two null fields: equals=" + firstNull.equals(secondNull));

        var positiveZero = new FloatSample(+0.0f);
        var negativeZero = new FloatSample(-0.0f);
        if (!positiveZero.equals(negativeZero)) throw new AssertionError("Expected primitive zero equality");
        if (positiveZero.hashCode() == negativeZero.hashCode()) throw new AssertionError("Expected zero hash mismatch");
        System.out.println("signed float zero: equals=" + positiveZero.equals(negativeZero)
                + ", hashCode=" + positiveZero.hashCode() + " / " + negativeZero.hashCode());
    }
}

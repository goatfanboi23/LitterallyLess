package software.enginer.litterallyless.util;

public abstract class ConditionalFunction {

    public boolean evaluate() {
        return true;
    }

    public boolean run() {
        if (evaluate()) {
            onSuccess();
            return true;
        } else {
            onFailure();
            return false;
        }
    }
    public void onSuccess() {}

    public void onFailure() {}
}

package org.ivy.settlement.infrastructure.anyhow;

/**
 * description:
 * @author carrot
 */
public class ProcessResult<T> {

    public final static ProcessResult<Void> SUCCESSFUL = ofSuccess(null);

    public final static ProcessResult<Void> ERROR = ofError();

    public final Status status;

    public final T result;

    public final StringBuilder errBuild;

    private ProcessResult(Status status, T result, String errMsg) {
        this.status = status;
        this.result = result;
        this.errBuild = new StringBuilder(errMsg == null? "": errMsg);
    }

    public enum Status {
        SUCCESS,
        ERROR;
    }

    public boolean isSuccess() {
        return this.status == Status.SUCCESS;
    }

    public T getResult() {
        return result;
    }

    public String getErrMsg() {
        return this.errBuild.toString();
    }

    public ProcessResult<T> appendErrorMsg(String errorMsg) {
        errBuild.append("\n")
                .append(errorMsg);
        return this;
    }

    public static <T> ProcessResult<T>  ofSuccess(T result) {
        return new ProcessResult<>(Status.SUCCESS, result, "");
    }

    public static <T> ProcessResult<T>  ofSuccess() {
        return new ProcessResult<>(Status.SUCCESS, null, "");
    }

    public static <T> ProcessResult<T>  ofError(String errMsg) {
        return new ProcessResult<>(Status.ERROR, null, errMsg);
    }

    public static <T> ProcessResult<T>  ofError() {
        return new ProcessResult<>(Status.ERROR, null, "");
    }
}

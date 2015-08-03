package cn.momia.mapi.common.exception;

public class MomiaException extends RuntimeException {
    public MomiaException() {}

    public MomiaException(String msg) {
        super(msg);
    }
}

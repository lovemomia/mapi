package cn.momia.mapi.api;

import cn.momia.mapi.common.exception.MomiaExpiredException;
import cn.momia.mapi.common.exception.MomiaFailedException;
import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.api.base.exception.MomiaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;

public abstract class AbstractApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractApi.class);

    @ExceptionHandler
    public ResponseMessage exception(Exception exception) {
        LOGGER.error("exception!!", exception);

        if (exception instanceof MomiaFailedException || exception instanceof MomiaException) {
            return ResponseMessage.FAILED(exception.getMessage());
        } else if (exception instanceof MomiaExpiredException) {
            return ResponseMessage.TOKEN_EXPIRED;
        } else if (exception instanceof MissingServletRequestParameterException) {
            return ResponseMessage.BAD_REQUEST;
        } else {
            return ResponseMessage.INTERNAL_SERVER_ERROR;
        }
    }
}

package cn.momia.mapi.api;

import cn.momia.api.base.exception.MomiaExpiredException;
import cn.momia.api.base.exception.MomiaFailedException;
import cn.momia.api.base.exception.MomiaException;
import cn.momia.api.base.http.MomiaHttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractApi.class);

    protected static int CLIENT_TYPE_WAP = 1;
    protected static int CLIENT_TYPE_APP = 2;

    protected static final int IMAGE_LARGE = 1;
    protected static final int IMAGE_MIDDLE = 2;
    protected static final int IMAGE_SMALL = 3;

    protected int getClientType(HttpServletRequest request) {
        return StringUtils.isBlank(request.getParameter("terminal")) ? CLIENT_TYPE_WAP : CLIENT_TYPE_APP;
    }

    @ExceptionHandler
    public MomiaHttpResponse exception(Exception exception) throws Exception {
        if (exception instanceof MomiaException) LOGGER.error("exception!!", exception);

        if (exception instanceof MomiaFailedException) {
            return MomiaHttpResponse.FAILED(exception.getMessage());
        } else if (exception instanceof MomiaExpiredException) {
            return MomiaHttpResponse.TOKEN_EXPIRED;
        } else {
            throw exception;
        }
    }
}

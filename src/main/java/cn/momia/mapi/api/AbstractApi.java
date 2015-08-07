package cn.momia.mapi.api;

import cn.momia.mapi.common.config.Configuration;
import cn.momia.mapi.common.exception.MomiaExpiredException;
import cn.momia.mapi.common.exception.MomiaFailedException;
import cn.momia.mapi.common.http.MomiaHttpRequest;
import cn.momia.mapi.common.http.MomiaHttpRequestExecutor;
import cn.momia.mapi.common.http.MomiaHttpResponseCollector;
import cn.momia.mapi.web.response.ResponseMessage;
import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

public abstract class AbstractApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractApi.class);

    @Autowired protected MomiaHttpRequestExecutor requestExecutor;

    protected String url(Object... paths) {
        // TODO 根据paths判断使用哪个service
        StringBuilder urlBuilder = new StringBuilder().append(Configuration.getString("Service.Base"));
        for (Object path : paths) urlBuilder.append("/").append(path);

        return urlBuilder.toString();

    }

    protected ResponseMessage executeRequest(MomiaHttpRequest request) {
        return executeRequest(request, null);
    }

    protected ResponseMessage executeRequest(MomiaHttpRequest request, Function<Object, Object> buildResponseData) {
        ResponseMessage responseMessage = requestExecutor.execute(request);

        if (buildResponseData == null || !responseMessage.successful()) return responseMessage;
        return ResponseMessage.SUCCESS(buildResponseData.apply(responseMessage.getData()));
    }

    protected ResponseMessage executeRequests(List<MomiaHttpRequest> requests, Function<MomiaHttpResponseCollector, Object> buildResponseData) {
        MomiaHttpResponseCollector collector = requestExecutor.execute(requests);

        if (collector.isNotLogin()) return ResponseMessage.TOKEN_EXPIRED;
        if (!collector.isSuccessful()) {
            LOGGER.error("fail to execute requests: {}, exceptions: {}", requests, collector.getExceptions());
            return ResponseMessage.FAILED(collector.getErrmsg());
        }

        return ResponseMessage.SUCCESS(buildResponseData.apply(collector));
    }

    @ExceptionHandler
    public ResponseMessage exception(Exception exception) {
        LOGGER.error("exception!!", exception);

        if(exception instanceof MomiaFailedException) {
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

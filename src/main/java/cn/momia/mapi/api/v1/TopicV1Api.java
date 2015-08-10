package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.http.MomiaHttpRequest;
import cn.momia.mapi.web.response.ResponseMessage;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/topic")
public class TopicV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage topic(@RequestParam long id) {
        if(id <= 0) return ResponseMessage.BAD_REQUEST;

        MomiaHttpRequest request = MomiaHttpRequest.GET(url("topic", id));

        return executeRequest(request, new Function<Object, Object>() {
            @Override
            public Object apply(Object data) {
                JSONArray groupedProductsJson = (JSONArray) data;
                for (int i = 0; i < groupedProductsJson.size(); i++) {
                    JSONObject productsJson = groupedProductsJson.getJSONObject(i);
                    productsFunc.apply(productsJson.get("products"));
                }

                return data;
            }
        });
    }
}

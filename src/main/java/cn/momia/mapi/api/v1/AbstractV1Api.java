package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.config.Configuration;
import cn.momia.mapi.common.exception.MomiaExpiredException;
import cn.momia.mapi.common.http.MomiaHttpParamBuilder;
import cn.momia.mapi.common.http.MomiaHttpRequest;
import cn.momia.mapi.common.img.ImageFile;
import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AbstractV1Api extends AbstractApi {
    protected Function<Object, Object> userFunc = new Function<Object, Object>() {
        @Override
        public Object apply(Object data) {
            JSONObject userJson = (JSONObject) data;
            userJson.put("avatar", ImageFile.url(userJson.getString("avatar")));

            return data;
        }
    };

    protected Function<Object, Object> pagedUsersFunc = new Function<Object, Object>() {
        @Override
        public Object apply(Object data) {
            JSONArray usersJson = ((JSONObject) data).getJSONArray("list");
            for (int i = 0; i < usersJson.size(); i++) {
                userFunc.apply(usersJson.getJSONObject(i));
            }

            return data;
        }
    };

    protected Function<Object, Object> pagedFeedCommentsFunc = new Function<Object, Object>() {
        @Override
        public Object apply(Object data) {
            JSONArray feedCommentsJson = ((JSONObject) data).getJSONArray("list");
            for (int i = 0; i < feedCommentsJson.size(); i++) {
                JSONObject feedCommentJson = feedCommentsJson.getJSONObject(i);
                feedCommentJson.put("avatar", ImageFile.url(feedCommentJson.getString("avatar")));
            }

            return data;
        }
    };

    protected Function<Object, Object> feedFunc = new Function<Object, Object>() {
        @Override
        public Object apply(Object data) {
            JSONObject feedJson = (JSONObject) data;
            feedJson.put("avatar", ImageFile.url(feedJson.getString("avatar")));
            if (feedJson.containsKey("imgs")) feedJson.put("imgs", processFeedImgs(feedJson.getJSONArray("imgs")));

            return data;
        }
    };

    private static List<String> processFeedImgs(JSONArray imgsJson) {
        List<String> imgs = new ArrayList<String>();
        for (int i = 0; i < imgsJson.size(); i++) {
            imgs.add(ImageFile.middleUrl(imgsJson.getString(i)));
        }

        return imgs;
    }

    protected Function<Object, Object> pagedFeedsFunc = new Function<Object, Object>() {
        @Override
        public Object apply(Object data) {
            JSONArray feedsJson = ((JSONObject) data).getJSONArray("list");
            for (int i = 0; i < feedsJson.size(); i++) {
                JSONObject feedJson = feedsJson.getJSONObject(i);
                feedFunc.apply(feedJson);
            }

            return data;
        }
    };

    protected Function<Object, Object> productFunc = new Function<Object, Object>() {
        @Override
        public Object apply(Object data) {
            JSONObject productJson = (JSONObject) data;
            productJson.put("url", buildUrl(productJson.getLong("id")));
            productJson.put("thumb", ImageFile.smallUrl(productJson.getString("thumb")));
            if (productJson.containsKey("cover")) productJson.put("cover", ImageFile.middleUrl(productJson.getString("cover")));
            if (productJson.containsKey("imgs")) productJson.put("imgs", processProductImgs(productJson.getJSONArray("imgs")));
            if (productJson.containsKey("content")) productJson.put("content", processContent(productJson.getJSONArray("content")));

            return data;
        }
    };

    private String buildUrl(long id) {
        return Configuration.getString("Product.Url") + "?id=" + id;
    }

    private static List<String> processProductImgs(JSONArray imgsJson) {
        List<String> imgs = new ArrayList<String>();
        for (int i = 0; i < imgsJson.size(); i++) {
            imgs.add(ImageFile.largeUrl(imgsJson.getString(i)));
        }

        return imgs;
    }

    private static JSONArray processContent(JSONArray contentJson) {
        for (int i = 0; i < contentJson.size(); i++) {
            JSONObject contentBlockJson = contentJson.getJSONObject(i);
            JSONArray bodyJson = contentBlockJson.getJSONArray("body");
            for (int j = 0; j < bodyJson.size(); j++) {
                JSONObject bodyBlockJson = bodyJson.getJSONObject(j);
                String img = bodyBlockJson.getString("img");
                if (!StringUtils.isBlank(img)) bodyBlockJson.put("img", ImageFile.largeUrl(img));
            }
        }

        return contentJson;
    }

    protected Function<Object, Object> productsFunc = new Function<Object, Object>() {
        @Override
        public Object apply(Object data) {
            JSONArray productsJson = (JSONArray) data;
            for (int i = 0; i < productsJson.size(); i++) {
                JSONObject productJson = productsJson.getJSONObject(i);
                productFunc.apply(productJson);
            }

            return data;
        }
    };

    protected Function<Object, Object> pagedProductsFunc = new Function<Object, Object>() {
        @Override
        public Object apply(Object data) {
            JSONArray productsJson = ((JSONObject) data).getJSONArray("list");
            productsFunc.apply(productsJson);

            return data;
        }
    };

    protected Function<Object, Object> orderDetailFunc = new Function<Object, Object>() {
        @Override
        public Object apply(Object data) {
            JSONObject orderDetailJson = (JSONObject) data;
            orderDetailJson.put("cover", ImageFile.middleUrl(orderDetailJson.getString("cover")));

            return data;
        }
    };

    protected Function<Object, Object> pagedOrdersFunc = new Function<Object, Object>() {
        @Override
        public Object apply(Object data) {
            JSONArray ordersJson = ((JSONObject) data).getJSONArray("list");
            for (int i = 0; i < ordersJson.size(); i++) {
                orderDetailFunc.apply(ordersJson.getJSONObject(i));
            }

            return data;
        }
    };

    protected long getUserId(String utoken) {
        MomiaHttpParamBuilder builder = new MomiaHttpParamBuilder().add("utoken", utoken);
        MomiaHttpRequest request = MomiaHttpRequest.GET(url("user"), builder.build());

        ResponseMessage response = executeRequest(request);
        if (response.successful()) return ((JSONObject) response.getData()).getLong("id");

        throw new MomiaExpiredException();
    }
}

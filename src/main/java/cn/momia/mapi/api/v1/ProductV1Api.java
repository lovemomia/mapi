package cn.momia.mapi.api.v1;

import cn.momia.api.base.MetaUtil;
import cn.momia.api.user.leader.Leader;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.product.comment.Comment;
import cn.momia.api.product.comment.PagedComments;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.api.deal.DealServiceApi;
import cn.momia.api.product.ProductServiceApi;
import cn.momia.api.product.Product;
import cn.momia.api.product.sku.Sku;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.User;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/v1/product")
public class ProductV1Api extends AbstractV1Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductV1Api.class);

    @RequestMapping(value = "/weekend", method = RequestMethod.GET)
    public MomiaHttpResponse listByWeekend(@RequestParam(value = "city") int cityId, @RequestParam int start) {
        if (cityId < 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processPagedProducts(ProductServiceApi.PRODUCT.listByWeekend(cityId, start, Configuration.getInt("PageSize.Product")), IMAGE_MIDDLE));
    }

    @RequestMapping(value = "/month", method = RequestMethod.GET)
    public MomiaHttpResponse listByMonth(@RequestParam(value = "city") int cityId, @RequestParam int month) {
        if (cityId < 0 || month <= 0 || month > 12) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processGroupedProducts(ProductServiceApi.PRODUCT.listByMonth(cityId, month), IMAGE_MIDDLE));
    }

    @RequestMapping(value = "/leader", method = RequestMethod.GET)
    public MomiaHttpResponse listNeedLeader(@RequestParam(value = "city") int cityId, @RequestParam int start) {
        if (cityId < 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processPagedProducts(ProductServiceApi.PRODUCT.listNeedLeader(cityId, start, Configuration.getInt("PageSize.Product")), IMAGE_MIDDLE));
    }

    @RequestMapping(value = "/sku/leader", method = RequestMethod.GET)
    public MomiaHttpResponse listSkusNeedLeader(@RequestParam(value = "pid") long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        Product product = ProductServiceApi.PRODUCT.get(id, Product.Type.BASE);
        List<Sku> skus = ProductServiceApi.SKU.listWithLeader(id);

        Set<Long> leaderUserIds = new HashSet<Long>();
        for (Sku sku : skus) {
            if (sku.getLeaderUserId() > 0) leaderUserIds.add(sku.getLeaderUserId());
        }
        List<Leader> leaders = UserServiceApi.LEADER.list(leaderUserIds);
        Map<Long, Leader> leadersMap = new HashMap<Long, Leader>();
        for (Leader leader : leaders) leadersMap.put(leader.getUserId(), leader);
        for (Sku sku : skus) {
            if (!sku.isNeedLeader()) {
                sku.setLeaderInfo("本场不需要领队");
            } else {
                Leader leader = leadersMap.get(sku.getLeaderUserId());
                if (leader == null || StringUtils.isBlank(leader.getName())) sku.setLeaderInfo("");
                else sku.setLeaderInfo(leader.getName() + "已成为领队");
            }
        }

        JSONObject productSkusJson = new JSONObject();
        productSkusJson.put("product", processProduct(product, IMAGE_MIDDLE));
        productSkusJson.put("skus", skus);

        return MomiaHttpResponse.SUCCESS(productSkusJson);
    }

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam(defaultValue = "") String utoken, @RequestParam long id, HttpServletRequest request) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        Product product = processProduct(ProductServiceApi.PRODUCT.get(id, Product.Type.FULL), utoken, getClientType(request));
        JSONObject productJson = JSON.parseObject(JSON.toJSONString(product));
        try {
            List<String> avatars = DealServiceApi.ORDER.listCustomerAvatars(id, Configuration.getInt("PageSize.ProductCustomer"));
            productJson.put("customers", buildCustomers(avatars, product.getStock()));

            productJson.put("comments", listComments(id, 0, Configuration.getInt("PageSize.ProductDetailComment")));

            long userId = StringUtils.isBlank(utoken) ? 0 : UserServiceApi.USER.get(utoken).getId();
            if (ProductServiceApi.PRODUCT.favored(userId, id)) productJson.put("favored", true);
        } catch (Exception e) {
            LOGGER.error("exception!!", e);
        }

        return MomiaHttpResponse.SUCCESS(productJson);
    }

    private PagedComments listComments(long id, int start, int count) {
        PagedComments pagedComments = ProductServiceApi.COMMENT.list(id, start, count);
        List<Long> userIds = new ArrayList<Long>();
        for (Comment comment : pagedComments.getList()) userIds.add(comment.getUserId());
        List<User> users = UserServiceApi.USER.list(userIds, User.Type.MINI);
        Map<Long, User> usersMap = new HashMap<Long, User>();
        for (User user : users) usersMap.put(user.getId(), user);
        for (Comment comment : pagedComments.getList()) {
            User user = usersMap.get(comment.getUserId());
            if (user == null || !user.exists()) {
                comment.setNickName("");
                comment.setAvatar("");
            } else {
                comment.setNickName(user.getNickName());
                comment.setAvatar(ImageFile.smallUrl(user.getAvatar()));
            }
        }

        return processPagedComments(pagedComments);
    }

    private JSONObject buildCustomers(List<String> avatars, int stock) {
        JSONObject customersJson = new JSONObject();
        customersJson.put("text", "玩伴信息" + ((stock > 0 && stock <= Configuration.getInt("Product.StockAlert")) ? "（仅剩" + stock + "个名额）" : ""));
        customersJson.put("avatars", processAvatars(avatars));

        return customersJson;
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public MomiaHttpResponse getDetail(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processProductDetail(ProductServiceApi.PRODUCT.getDetail(id)));
    }

    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public MomiaHttpResponse placeOrder(@RequestParam String utoken, @RequestParam long id) {
        if(StringUtils.isBlank(utoken) || id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject placeOrderJson = new JSONObject();
        placeOrderJson.put("contacts", UserServiceApi.USER.getContacts(utoken));
        List<Sku> skus = ProductServiceApi.SKU.list(id, Sku.Status.AVALIABLE);
        placeOrderJson.put("places", extractPlaces(skus));
        placeOrderJson.put("skus", skus);

        return MomiaHttpResponse.SUCCESS(placeOrderJson);
    }

    private JSONArray extractPlaces(List<Sku> skus) {
        JSONArray placesJson = new JSONArray();
        Set<Integer> placeIds = new HashSet<Integer>();
        for (Sku sku : skus) {
            int placeId = sku.getPlaceId();
            if (placeId <= 0 || placeIds.contains(placeId)) continue;
            placeIds.add(placeId);
            
            JSONObject placeJson = new JSONObject();
            placeJson.put("id", placeId);
            placeJson.put("name", sku.getPlaceName());
            placeJson.put("region", MetaUtil.getRegionName(sku.getRegionId()));
            placeJson.put("address", sku.getAddress());

            placesJson.add(placeJson);
        }
        return placesJson;
    }

    @RequestMapping(value = "/playmate", method = RequestMethod.GET)
    public MomiaHttpResponse listPlaymates(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processPlaymates(DealServiceApi.ORDER.listPlaymates(id, Configuration.getInt("PageSize.PlaymateSku"))));
    }

    @RequestMapping(value = "/favor", method = RequestMethod.POST)
    public MomiaHttpResponse favor(@RequestParam String utoken, @RequestParam long id){
        if (StringUtils.isBlank(utoken) || id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = UserServiceApi.USER.get(utoken);
        ProductServiceApi.PRODUCT.favor(user.getId(), id);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/unfavor", method = RequestMethod.POST)
    public MomiaHttpResponse unfavor(@RequestParam String utoken, @RequestParam long id){
        if (StringUtils.isBlank(utoken) || id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = UserServiceApi.USER.get(utoken);
        ProductServiceApi.PRODUCT.unfavor(user.getId(), id);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public MomiaHttpResponse addComment(@RequestParam String utoken, @RequestParam String comment) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(comment)) return MomiaHttpResponse.BAD_REQUEST;

        User user = UserServiceApi.USER.get(utoken);
        JSONObject commentJson = JSON.parseObject(comment);
        commentJson.put("userId", user.getId());

        long orderId = commentJson.getLong("orderId");
        long productId = commentJson.getLong("productId");
        long skuId = commentJson.getLong("skuId");
        if (!DealServiceApi.ORDER.check(utoken, orderId, productId, skuId)) return MomiaHttpResponse.FAILED("您只能对自己参加过的活动发表评论");

        ProductServiceApi.COMMENT.add(commentJson);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/comment", method = RequestMethod.GET)
    public MomiaHttpResponse listComments(@RequestParam long id, @RequestParam int start) {
        if (id <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(listComments(id, start, Configuration.getInt("PageSize.ProductComment")));
    }
}

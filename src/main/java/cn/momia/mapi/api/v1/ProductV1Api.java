package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.config.Configuration;
import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.api.deal.DealServiceApi;
import cn.momia.api.product.ProductServiceApi;
import cn.momia.api.product.Product;
import cn.momia.api.product.sku.Sku;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.User;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/product")
public class ProductV1Api extends AbstractV1Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductV1Api.class);

    @Autowired private DealServiceApi dealServiceApi;
    @Autowired private ProductServiceApi productServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/weekend", method = RequestMethod.GET)
    public ResponseMessage listByWeekend(@RequestParam(value = "city") int cityId, @RequestParam int start) {
        if (cityId < 0 || start < 0) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processPagedProducts(productServiceApi.PRODUCT.listByWeekend(cityId, start, Configuration.getInt("PageSize.Product"))));
    }

    @RequestMapping(value = "/month", method = RequestMethod.GET)
    public ResponseMessage listByMonth(@RequestParam(value = "city") int cityId, @RequestParam int month) {
        if (cityId < 0 || month <= 0 || month > 12) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processGroupedProducts(productServiceApi.PRODUCT.listByMonth(cityId, month)));
    }

    @RequestMapping(value = "/leader", method = RequestMethod.GET)
    public ResponseMessage listNeedLeader(@RequestParam(value = "city") int cityId, @RequestParam int start) {
        if (cityId < 0 || start < 0) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processPagedProducts(productServiceApi.PRODUCT.listNeedLeader(cityId, start, Configuration.getInt("PageSize.Product"))));
    }

    @RequestMapping(value = "/sku/leader", method = RequestMethod.GET)
    public ResponseMessage listSkusNeedLeader(@RequestParam(value = "pid") long id) {
        if (id <= 0) return ResponseMessage.BAD_REQUEST;

        Product product = productServiceApi.PRODUCT.get(id, false);
        List<Sku> skus = productServiceApi.SKU.listWithLeader(id);

        JSONObject productSkusJson = new JSONObject();
        productSkusJson.put("product", processProduct(product));
        productSkusJson.put("skus", skus);

        return ResponseMessage.SUCCESS(productSkusJson);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage get(@RequestParam(defaultValue = "") String utoken, @RequestParam long id) {
        if (id <= 0) return ResponseMessage.BAD_REQUEST;

        Product product = processProduct(productServiceApi.PRODUCT.get(id, true));
        if (!product.isOpened()) product.setSoldOut(true);

        JSONObject productJson = JSON.parseObject(JSON.toJSONString(product));
        try {
            List<String> avatars = dealServiceApi.ORDER.listCustomerAvatars(id, Configuration.getInt("PageSize.ProductCustomer"));
            productJson.put("customers", buildCustomers(avatars, product.getStock()));

            long userId = StringUtils.isBlank(utoken) ? 0 : userServiceApi.USER.get(utoken).getId();
            if (productServiceApi.PRODUCT.favored(userId, id)) productJson.put("favored", true);
        } catch (Exception e) {
            LOGGER.error("exception!!", e);
        }

        return ResponseMessage.SUCCESS(productJson);
    }

    private JSONObject buildCustomers(List<String> avatars, int stock) {
        JSONObject customersJson = new JSONObject();
        customersJson.put("text", "玩伴信息" + ((stock > 0 && stock <= Configuration.getInt("Product.StockAlert")) ? "（仅剩" + stock + "个名额）" : ""));
        customersJson.put("avatars", processAvatars(avatars));

        return customersJson;
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public ResponseMessage getDetail(@RequestParam long id) {
        if (id <= 0) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(productServiceApi.PRODUCT.getDetail(id));
    }

    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public ResponseMessage placeOrder(@RequestParam String utoken, @RequestParam long id) {
        if(StringUtils.isBlank(utoken) || id <= 0) return ResponseMessage.BAD_REQUEST;

        JSONObject placeOrderJson = new JSONObject();
        placeOrderJson.put("contacts", userServiceApi.USER.getContacts(utoken));
        placeOrderJson.put("skus", productServiceApi.SKU.list(id));

        return ResponseMessage.SUCCESS(placeOrderJson);
    }

    @RequestMapping(value = "/playmate", method = RequestMethod.GET)
    public ResponseMessage listPlaymates(@RequestParam long id) {
        if (id <= 0) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processPlaymates(dealServiceApi.ORDER.listPlaymates(id, Configuration.getInt("PageSize.PlaymateSku"))));
    }

    @RequestMapping(value = "/favor", method = RequestMethod.POST)
    public ResponseMessage favor(@RequestParam String utoken, @RequestParam long id){
        if (StringUtils.isBlank(utoken) || id <= 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        productServiceApi.PRODUCT.favor(user.getId(), id);

        return ResponseMessage.SUCCESS;
    }

    @RequestMapping(value = "/unfavor", method = RequestMethod.POST)
    public ResponseMessage unfavor(@RequestParam String utoken, @RequestParam long id){
        if (StringUtils.isBlank(utoken) || id <= 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        productServiceApi.PRODUCT.unfavor(user.getId(), id);

        return ResponseMessage.SUCCESS;
    }
}

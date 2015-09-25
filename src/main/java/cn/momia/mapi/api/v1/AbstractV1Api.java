package cn.momia.mapi.api.v1;

import cn.momia.api.user.UserServiceApi;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.client.ClientType;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.AbstractApi;
import cn.momia.api.product.dto.OrderDto;
import cn.momia.api.product.dto.ProductDto;
import cn.momia.api.user.dto.UserDto;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class AbstractV1Api extends AbstractApi {
    protected ProductDto processProduct(ProductDto product, int size) {
        return processProduct(product, size, ClientType.WAP);
    }

    protected ProductDto processProduct(ProductDto product, int size, int clientType) {
        product.setUrl(buildUrl(product.getId()));
        product.setThumb(ImageFile.smallUrl(product.getThumb()));

        if (!StringUtils.isBlank(product.getCover())) {
            if (size == ImageFile.Size.LARGE) product.setCover(ImageFile.largeUrl(product.getCover()));
            else if (size == ImageFile.Size.MIDDLE) product.setCover(ImageFile.middleUrl(product.getCover()));
            else product.setCover(ImageFile.smallUrl(product.getCover()));
        }

        if (product.getImgs() != null) processImgs(product.getImgs());
        if (product.getContent() != null) processContent(product.getContent(), clientType);

        return product;
    }

    private String buildUrl(long id) {
        return Configuration.getString("Product.Url") + "?id=" + id;
    }

    private List<String> processImgs(List<String> imgs) {
        for (int i = 0; i < imgs.size(); i++) imgs.set(i, ImageFile.largeUrl(imgs.get(i)));
        return imgs;
    }

    private JSONArray processContent(JSONArray contentJson, int clientType) {
        for (int i = 0; i < contentJson.size(); i++) {
            JSONObject contentBlockJson = contentJson.getJSONObject(i);
            JSONArray bodyJson = contentBlockJson.getJSONArray("body");
            for (int j = 0; j < bodyJson.size(); j++) {
                JSONObject bodyBlockJson = bodyJson.getJSONObject(j);
                String img = bodyBlockJson.getString("img");
                if (!StringUtils.isBlank(img)) bodyBlockJson.put("img", ImageFile.largeUrl(img));

                String link = bodyBlockJson.getString("link");
                if (!StringUtils.isBlank(link)) bodyBlockJson.put("link", buildLink(link, clientType));
            }
        }

        return contentJson;
    }

    protected ProductDto processProduct(ProductDto product, String utoken) {
        return processProduct(product, utoken, ClientType.WAP);
    }

    protected ProductDto processProduct(ProductDto product, String utoken, int clientType) {
        ProductDto processedProduct = processProduct(product, ImageFile.Size.LARGE, clientType);
        if (!StringUtils.isBlank(utoken)) {
            String inviteCode = UserServiceApi.USER.getInviteCode(utoken);
            if (!StringUtils.isBlank(inviteCode)) processedProduct.setUrl(processedProduct.getUrl() + "&invite=" + inviteCode);
        }

        return processedProduct;
    }

    protected List<ProductDto> processProducts(List<ProductDto> products) {
        return processProducts(products, ImageFile.Size.MIDDLE);
    }

    protected List<ProductDto> processProducts(List<ProductDto> products, int size) {
        for (ProductDto product : products) processProduct(product, size);
        return products;
    }

    protected PagedList<ProductDto> processPagedProducts(PagedList<ProductDto> products) {
        return processPagedProducts(products, ImageFile.Size.MIDDLE);
    }

    protected PagedList<ProductDto> processPagedProducts(PagedList<ProductDto> products, int size) {
        processProducts(products.getList(), size);
        return products;
    }

    protected UserDto processUser(UserDto user) {
        user.setAvatar(ImageFile.smallUrl(user.getAvatar()));
        return user;
    }

    protected PagedList<OrderDto> processPagedOrders(PagedList<OrderDto> orders) {
        for (OrderDto order : orders.getList()) processOrder(order);
        return orders;
    }

    protected OrderDto processOrder(OrderDto order) {
        order.setCover(ImageFile.middleUrl(order.getCover()));
        return order;
    }
}

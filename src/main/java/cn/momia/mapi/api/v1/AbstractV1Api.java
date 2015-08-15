package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.config.Configuration;
import cn.momia.mapi.common.img.ImageFile;
import cn.momia.mapi.common.util.MetaUtil;
import cn.momia.mapi.api.AbstractApi;
import cn.momia.service.deal.api.order.Order;
import cn.momia.service.deal.api.order.PagedOrders;
import cn.momia.service.deal.api.order.Playmate;
import cn.momia.service.deal.api.order.SkuPlaymates;
import cn.momia.service.feed.api.comment.FeedComment;
import cn.momia.service.feed.api.comment.PagedFeedComments;
import cn.momia.service.feed.api.star.FeedStar;
import cn.momia.service.feed.api.star.PagedFeedStars;
import cn.momia.service.product.api.product.PagedProducts;
import cn.momia.service.product.api.product.Product;
import cn.momia.service.product.api.product.ProductGroup;
import cn.momia.service.product.api.topic.Banner;
import cn.momia.service.product.api.topic.TopicGroup;
import cn.momia.service.product.api.topic.Topic;
import cn.momia.service.user.api.user.User;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class AbstractV1Api extends AbstractApi {
    protected User processUser(User user) {
        String avatar = user.getAvatar();
        if (!StringUtils.isBlank(avatar)) user.setAvatar(ImageFile.url(avatar));

        return user;
    }

    protected List<String> processAvatars(List<String> avatars) {
        for (int i = 0; i < avatars.size(); i++) {
            avatars.set(i, ImageFile.url(avatars.get(i)));
        }

        return avatars;
    }

    protected List<Banner> processBanners(List<Banner> banners) {
        for (Banner banner : banners) {
            processBanner(banner);
        }

        return banners;
    }

    private Banner processBanner(Banner banner) {
        banner.setCover(ImageFile.largeUrl(banner.getCover()));

        return banner;
    }

    protected Topic processTopic(Topic topic) {
        topic.setCover(ImageFile.largeUrl(topic.getCover()));

        for (TopicGroup topicGroup : topic.getGroups()) {
            processProducts(topicGroup.getProducts());
        }

        return topic;
    }

    protected Product processProduct(Product product) {
        product.setUrl(buildUrl(product.getId()));
        product.setThumb(ImageFile.smallUrl(product.getThumb()));

        if (product.getRegionId() != null) product.setRegion(MetaUtil.getRegionName(product.getRegionId()));

        if (!StringUtils.isBlank(product.getCover())) product.setCover(ImageFile.largeUrl(product.getCover()));
        if (product.getImgs() != null) processImgs(product.getImgs());
        if (product.getContent() != null) processContent(product.getContent());

        return product;
    }

    private List<String> processImgs(List<String> imgs) {
        for (int i = 0; i < imgs.size(); i++) {
            imgs.set(i, ImageFile.largeUrl(imgs.get(i)));
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

    protected List<Product> processProducts(List<Product> products) {
        for (Product product : products) {
            processProduct(product);
        }

        return products;
    }

    protected PagedProducts processPagedProducts(PagedProducts products) {
        processProducts(products.getList());

        return products;
    }

    protected List<ProductGroup> processGroupedProducts(List<ProductGroup> products) {
        for (ProductGroup productGroup : products) {
            processProducts(productGroup.getProducts());
        }

        return products;
    }

    protected PagedOrders processPagedOrders(PagedOrders orders) {
        for (Order order : orders.getList()) {
            processOrder(order);
        }

        return orders;
    }

    protected Order processOrder(Order order) {
        order.setCover(ImageFile.middleUrl(order.getCover()));

        return order;
    }

    protected List<SkuPlaymates> processPlaymates(List<SkuPlaymates> playmates) {
        for (SkuPlaymates skuPlaymates : playmates) {
            for (Playmate playmate : skuPlaymates.getPlaymates()) {
                playmate.setAvatar(ImageFile.url(playmate.getAvatar()));
            }
        }

        return playmates;
    }

    protected PagedFeedComments processPagedFeedComments(PagedFeedComments comments) {
        for (FeedComment feedComment : comments.getList()) {
            feedComment.setAvatar(ImageFile.url(feedComment.getAvatar()));
        }

        return comments;
    }

    protected PagedFeedStars processPagedFeedStars(PagedFeedStars stars) {
        for (FeedStar feedStar : stars.getList()) {
            feedStar.setAvatar(ImageFile.url(feedStar.getAvatar()));
        }

        return stars;
    }

    private String buildUrl(long id) {
        return Configuration.getString("Product.Url") + "?id=" + id;
    }
}

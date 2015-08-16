package cn.momia.mapi.api.v1;

import cn.momia.api.feed.Feed;
import cn.momia.api.feed.PagedFeeds;
import cn.momia.mapi.common.config.Configuration;
import cn.momia.mapi.common.img.ImageFile;
import cn.momia.mapi.common.util.MetaUtil;
import cn.momia.mapi.api.AbstractApi;
import cn.momia.api.deal.order.Order;
import cn.momia.api.deal.order.PagedOrders;
import cn.momia.api.deal.order.Playmate;
import cn.momia.api.deal.order.SkuPlaymates;
import cn.momia.api.feed.comment.FeedComment;
import cn.momia.api.feed.comment.PagedFeedComments;
import cn.momia.api.feed.star.FeedStar;
import cn.momia.api.feed.star.PagedFeedStars;
import cn.momia.api.product.PagedProducts;
import cn.momia.api.product.Product;
import cn.momia.api.product.ProductGroup;
import cn.momia.api.product.topic.Banner;
import cn.momia.api.product.topic.TopicGroup;
import cn.momia.api.product.topic.Topic;
import cn.momia.api.user.User;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class AbstractV1Api extends AbstractApi {
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

    protected PagedFeeds processPagedFeeds(PagedFeeds feeds) {
        for (Feed feed : feeds.getList()) {
            processFeed(feed);
        }

        return feeds;
    }

    protected Feed processFeed(Feed feed) {
        feed.setAvatar(ImageFile.url(feed.getAvatar()));
        if (feed.getImgs() != null) {
            for (int i = 0; i < feed.getImgs().size(); i++) {
                feed.getImgs().set(i, ImageFile.middleUrl(feed.getImgs().get(i)));
            }
        }

        return feed;
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

    private String buildUrl(long id) {
        return Configuration.getString("Product.Url") + "?id=" + id;
    }
}

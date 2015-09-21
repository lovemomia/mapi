package cn.momia.mapi.api.v1;

import cn.momia.api.feed.entity.Feed;
import cn.momia.api.product.entity.Comment;
import cn.momia.api.user.UserServiceApi;
import cn.momia.common.api.entity.PagedList;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.AbstractApi;
import cn.momia.api.deal.entity.Order;
import cn.momia.api.deal.entity.Playmate;
import cn.momia.api.deal.entity.SkuPlaymates;
import cn.momia.api.feed.entity.FeedComment;
import cn.momia.api.feed.entity.FeedStar;
import cn.momia.api.product.entity.Product;
import cn.momia.api.product.entity.ProductGroup;
import cn.momia.api.product.entity.Banner;
import cn.momia.api.product.entity.TopicGroup;
import cn.momia.api.product.entity.Topic;
import cn.momia.api.user.entity.User;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvm.hotspot.debugger.Page;

import java.net.URLEncoder;
import java.util.List;

public class AbstractV1Api extends AbstractApi {
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractApi.class);

    protected PagedList<Order> processPagedOrders(PagedList<Order> orders) {
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
                playmate.setAvatar(ImageFile.smallUrl(playmate.getAvatar()));
            }
        }

        return playmates;
    }

    protected PagedList<Feed> processPagedFeeds(PagedList<Feed> feeds) {
        for (Feed feed : feeds.getList()) {
            processFeed(feed);
        }

        return feeds;
    }

    protected Feed processFeed(Feed feed) {
        feed.setAvatar(ImageFile.smallUrl(feed.getAvatar()));
        if (feed.getImgs() != null) {
            for (int i = 0; i < feed.getImgs().size(); i++) {
                feed.getImgs().set(i, ImageFile.middleUrl(feed.getImgs().get(i)));
            }
        }

        return feed;
    }

    protected PagedList<FeedComment> processPagedFeedComments(PagedList<FeedComment> comments) {
        for (FeedComment feedComment : comments.getList()) {
            feedComment.setAvatar(ImageFile.smallUrl(feedComment.getAvatar()));
        }

        return comments;
    }

    protected PagedList<FeedStar> processPagedFeedStars(PagedList<FeedStar> stars) {
        for (FeedStar feedStar : stars.getList()) {
            feedStar.setAvatar(ImageFile.smallUrl(feedStar.getAvatar()));
        }

        return stars;
    }

    protected List<Banner> processBanners(List<Banner> banners, int clientType) {
        for (Banner banner : banners) {
            processBanner(banner, clientType);
        }

        return banners;
    }

    private Banner processBanner(Banner banner, int clientType) {
        banner.setCover(ImageFile.url(banner.getCover()));
        banner.setAction(buildLink(banner.getAction(), clientType));

        return banner;
    }

    private String buildLink(String link, int clientType) {
        if (clientType == CLIENT_TYPE_APP) return Configuration.getString("AppConf.Name") + "://web?url=" + URLEncoder.encode(fullUrl(link, clientType));
        return fullUrl(link, clientType);
    }

    private String fullUrl(String link, int clientType) {
        if (link.startsWith("/")) {
            if (clientType == CLIENT_TYPE_APP) return Configuration.getString("AppConf.WapDomain") + link;
            return Configuration.getString("AppConf.WapDomain") + "/m" + link;
        }

        return link;
    }

    protected Topic processTopic(Topic topic) {
        topic.setCover(ImageFile.url(topic.getCover()));

        for (TopicGroup topicGroup : topic.getGroups()) {
            processProducts(topicGroup.getProducts());
        }

        return topic;
    }

    protected Product processProduct(Product product) {
        return processProduct(product, IMAGE_LARGE);
    }

    protected Product processProduct(Product product, int size) {
        return processProduct(product, size, CLIENT_TYPE_WAP);
    }

    protected Product processProduct(Product product, int size, int clientType) {
        product.setUrl(buildUrl(product.getId()));
        product.setThumb(ImageFile.smallUrl(product.getThumb()));

        if (!StringUtils.isBlank(product.getCover())) {
            if (size == IMAGE_LARGE) product.setCover(ImageFile.largeUrl(product.getCover()));
            else if (size == IMAGE_MIDDLE) product.setCover(ImageFile.middleUrl(product.getCover()));
            else product.setCover(ImageFile.smallUrl(product.getCover()));
        }

        if (product.getImgs() != null) processImgs(product.getImgs());
        if (product.getContent() != null) processContent(product.getContent(), clientType);

        return product;
    }

    protected Product processProduct(Product product, String utoken) {
        return processProduct(product, utoken, CLIENT_TYPE_WAP);
    }

    protected Product processProduct(Product product, String utoken, int clientType) {
        Product processedProduct = processProduct(product, IMAGE_LARGE, clientType);
        try {
            if (!StringUtils.isBlank(utoken)) {
                String inviteCode = UserServiceApi.USER.getInviteCode(utoken);
                if (!StringUtils.isBlank(inviteCode)) processedProduct.setUrl(processedProduct.getUrl() + "&invite=" + inviteCode);
            }
        } catch (Exception e) {
            LOGGER.error("fail to generate invite url");
        }

        return processedProduct;
    }

    private List<String> processImgs(List<String> imgs) {
        for (int i = 0; i < imgs.size(); i++) {
            imgs.set(i, ImageFile.largeUrl(imgs.get(i)));
        }

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

    protected List<Product> processProducts(List<Product> products) {
        return processProducts(products, IMAGE_LARGE);
    }

    protected List<Product> processProducts(List<Product> products, int size) {
        for (Product product : products) {
            processProduct(product, size);
        }

        return products;
    }

    protected PagedList<Product> processPagedProducts(PagedList<Product> products) {
        return processPagedProducts(products, IMAGE_LARGE);
    }

    protected PagedList<Product> processPagedProducts(PagedList<Product> products, int size) {
        processProducts(products.getList(), size);

        return products;
    }

    protected List<ProductGroup> processGroupedProducts(List<ProductGroup> products) {
        return processGroupedProducts(products, IMAGE_LARGE);
    }

    protected List<ProductGroup> processGroupedProducts(List<ProductGroup> products, int size) {
        for (ProductGroup productGroup : products) {
            processProducts(productGroup.getProducts(), size);
        }

        return products;
    }

    protected String processProductDetail(String detail) {
        Document detailDoc = Jsoup.parse(detail);
        Elements imgs = detailDoc.select("img[src]");
        for (Element element : imgs) {
            String imgUrl = element.attr("src");
            element.attr("src", ImageFile.largeUrl(imgUrl));
        }

        return detailDoc.toString();
    }

    protected PagedList<Comment> processPagedComments(PagedList<Comment> pagedComments) {
        for (Comment comment : pagedComments.getList()) {
            processComment(comment);
        }

        return pagedComments;
    }

    private Comment processComment(Comment comment) {
        for (int i = 0; i < comment.getImgs().size(); i++) {
            comment.getImgs().set(i, ImageFile.middleUrl(comment.getImgs().get(i)));
        }

        return comment;
    }

    protected User processUser(User user) {
        String avatar = user.getAvatar();
        if (!StringUtils.isBlank(avatar)) user.setAvatar(ImageFile.smallUrl(avatar));

        return user;
    }

    protected List<String> processAvatars(List<String> avatars) {
        for (int i = 0; i < avatars.size(); i++) {
            avatars.set(i, ImageFile.smallUrl(avatars.get(i)));
        }

        return avatars;
    }

    private String buildUrl(long id) {
        return Configuration.getString("Product.Url") + "?id=" + id;
    }
}

package cn.momia.mapi.api.v1;

import cn.momia.api.feed.dto.FeedDto;
import cn.momia.api.product.dto.CommentDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.common.api.entity.PagedList;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.AbstractApi;
import cn.momia.api.deal.dto.OrderDto;
import cn.momia.api.deal.dto.PlaymateDto;
import cn.momia.api.deal.dto.SkuPlaymatesDto;
import cn.momia.api.feed.dto.FeedCommentDto;
import cn.momia.api.feed.dto.FeedStarDto;
import cn.momia.api.product.dto.ProductDto;
import cn.momia.api.product.dto.ProductGroupDto;
import cn.momia.api.product.dto.BannerDto;
import cn.momia.api.product.dto.TopicGroupDto;
import cn.momia.api.product.dto.TopicDto;
import cn.momia.api.user.dto.UserDto;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.List;

public class AbstractV1Api extends AbstractApi {
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractApi.class);

    protected PagedList<OrderDto> processPagedOrders(PagedList<OrderDto> orders) {
        for (OrderDto order : orders.getList()) {
            processOrder(order);
        }

        return orders;
    }

    protected OrderDto processOrder(OrderDto order) {
        order.setCover(ImageFile.middleUrl(order.getCover()));

        return order;
    }

    protected List<SkuPlaymatesDto> processPlaymates(List<SkuPlaymatesDto> playmates) {
        for (SkuPlaymatesDto skuPlaymates : playmates) {
            for (PlaymateDto playmate : skuPlaymates.getPlaymates()) {
                playmate.setAvatar(ImageFile.smallUrl(playmate.getAvatar()));
            }
        }

        return playmates;
    }

    protected PagedList<FeedDto> processPagedFeeds(PagedList<FeedDto> feeds) {
        for (FeedDto feed : feeds.getList()) {
            processFeed(feed);
        }

        return feeds;
    }

    protected FeedDto processFeed(FeedDto feed) {
        feed.setAvatar(ImageFile.smallUrl(feed.getAvatar()));
        if (feed.getImgs() != null) {
            for (int i = 0; i < feed.getImgs().size(); i++) {
                feed.getImgs().set(i, ImageFile.middleUrl(feed.getImgs().get(i)));
            }
        }

        return feed;
    }

    protected PagedList<FeedCommentDto> processPagedFeedComments(PagedList<FeedCommentDto> comments) {
        for (FeedCommentDto feedComment : comments.getList()) {
            feedComment.setAvatar(ImageFile.smallUrl(feedComment.getAvatar()));
        }

        return comments;
    }

    protected PagedList<FeedStarDto> processPagedFeedStars(PagedList<FeedStarDto> stars) {
        for (FeedStarDto feedStar : stars.getList()) {
            feedStar.setAvatar(ImageFile.smallUrl(feedStar.getAvatar()));
        }

        return stars;
    }

    protected List<BannerDto> processBanners(List<BannerDto> banners, int clientType) {
        for (BannerDto banner : banners) {
            processBanner(banner, clientType);
        }

        return banners;
    }

    private BannerDto processBanner(BannerDto banner, int clientType) {
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

    protected TopicDto processTopic(TopicDto topic) {
        topic.setCover(ImageFile.url(topic.getCover()));

        for (TopicGroupDto topicGroup : topic.getGroups()) {
            processProducts(topicGroup.getProducts());
        }

        return topic;
    }

    protected ProductDto processProduct(ProductDto product) {
        return processProduct(product, IMAGE_LARGE);
    }

    protected ProductDto processProduct(ProductDto product, int size) {
        return processProduct(product, size, CLIENT_TYPE_WAP);
    }

    protected ProductDto processProduct(ProductDto product, int size, int clientType) {
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

    protected ProductDto processProduct(ProductDto product, String utoken) {
        return processProduct(product, utoken, CLIENT_TYPE_WAP);
    }

    protected ProductDto processProduct(ProductDto product, String utoken, int clientType) {
        ProductDto processedProduct = processProduct(product, IMAGE_LARGE, clientType);
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

    protected List<ProductDto> processProducts(List<ProductDto> products) {
        return processProducts(products, IMAGE_LARGE);
    }

    protected List<ProductDto> processProducts(List<ProductDto> products, int size) {
        for (ProductDto product : products) {
            processProduct(product, size);
        }

        return products;
    }

    protected PagedList<ProductDto> processPagedProducts(PagedList<ProductDto> products) {
        return processPagedProducts(products, IMAGE_LARGE);
    }

    protected PagedList<ProductDto> processPagedProducts(PagedList<ProductDto> products, int size) {
        processProducts(products.getList(), size);

        return products;
    }

    protected List<ProductGroupDto> processGroupedProducts(List<ProductGroupDto> products) {
        return processGroupedProducts(products, IMAGE_LARGE);
    }

    protected List<ProductGroupDto> processGroupedProducts(List<ProductGroupDto> products, int size) {
        for (ProductGroupDto productGroup : products) {
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

    protected PagedList<CommentDto> processPagedComments(PagedList<CommentDto> pagedComments) {
        for (CommentDto comment : pagedComments.getList()) {
            processComment(comment);
        }

        return pagedComments;
    }

    private CommentDto processComment(CommentDto comment) {
        for (int i = 0; i < comment.getImgs().size(); i++) {
            comment.getImgs().set(i, ImageFile.middleUrl(comment.getImgs().get(i)));
        }

        return comment;
    }

    protected UserDto processUser(UserDto user) {
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

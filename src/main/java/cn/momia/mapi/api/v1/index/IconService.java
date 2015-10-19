package cn.momia.mapi.api.v1.index;

import cn.momia.common.service.DbAccessService;

import java.util.List;

public class IconService extends DbAccessService {
    public List<Icon> list(int cityId) {
        String sql = "SELECT Title, Img, Action FROM SG_Icon WHERE (CityId=? OR CityId=0) AND Status=1 ORDER BY Weight DESC, AddTime DESC";
        return queryList(sql, new Object[] { cityId }, Icon.class);
    }
}

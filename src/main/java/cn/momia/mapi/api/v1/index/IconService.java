package cn.momia.mapi.api.v1.index;

import cn.momia.common.service.DbAccessService;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IconService extends DbAccessService {
    public List<Icon> list(int cityId) {
        final List<Icon> icons = new ArrayList<Icon>();
        String sql = "SELECT Title, Img, Action FROM SG_Icon WHERE CityId=? AND Status=1 ORDER BY Weight DESC, AddTime DESC";
        jdbcTemplate.query(sql, new Object[] { cityId }, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                Icon icon = new Icon();
                icon.setTitle(rs.getString("Title"));
                icon.setImg(rs.getString("Img"));
                icon.setAction(rs.getString("Action"));

                icons.add(icon);
            }
        });

        return icons;
    }
}

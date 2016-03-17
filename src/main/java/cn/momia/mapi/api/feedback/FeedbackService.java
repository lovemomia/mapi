package cn.momia.mapi.api.feedback;

import cn.momia.common.service.AbstractService;

public class FeedbackService extends AbstractService {
    public boolean add(String content, String contact) {
        String sql = "INSERT INTO SG_Feedback(Content, Contact, AddTime) VALUES(?, ?, NOW())";
        return update(sql, new Object[] { content, contact });
    }
}

package jip.jobs;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultJobStats implements JobStats {
    private Date createDate;
    private Date startDate;
    private Date endDate;

    public DefaultJobStats() {
        this.createDate = new Date();
    }

    public DefaultJobStats(Map config) {
        this.createDate = new Date(((Number) config.get("createDate")).longValue());
        if(config.containsKey("startDate")){
            this.startDate = new Date(((Number) config.get("startDate")).longValue());
        }
        if(config.containsKey("endDate")){
            this.endDate = new Date(((Number) config.get("endDate")).longValue());
        }
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public static Map<String, Object> toMap(JobStats stats){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("createDate", stats.getCreateDate().getTime());
        if(stats.getStartDate() != null){
            map.put("startDate", stats.getStartDate().getTime());
        }
        if(stats.getEndDate() != null){
            map.put("endDate", stats.getEndDate().getTime());
        }
        return map;
    }

}
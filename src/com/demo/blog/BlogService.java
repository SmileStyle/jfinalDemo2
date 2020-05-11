package com.demo.blog;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.demo.common.model.Blog;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法
 * 详见 JFinal 俱乐部: http://jfinal.com/club
 * 
 * BlogService
 * 所有 sql 与业务逻辑写在 Service 中，不要放在 Model 中，更不
 * 要放在 Controller 中，养成好习惯，有利于大型项目的开发与维护
 */
public class BlogService {
	
	/**
	 * 所有的 dao 对象也放在 Service 中
	 */
	private static final Blog dao = new Blog().dao();
	
	public Page<Blog> paginate(int pageNumber, int pageSize) {
		return dao.paginate(pageNumber, pageSize, "select *", "from blog order by id asc");
	}
	
	public Blog findById(int id) {
		return dao.findById(id);
	}
	
	public void deleteById(int id) {
		dao.deleteById(id);
	}
	
	
	public void normal(int[] a, int m) {
        Map<Integer,Integer> b = new HashMap<Integer, Integer>();
        for(int i = 0;i < a.length;i++) {
            for(int j = 0;j<a.length;j++) {
                if(i!=j&&(a[i]+a[j]==m)) {
                    if(a[i]>=a[j]) {
                        b.put(a[i], a[j]);
                    } else {
                        b.put(a[j], a[i]);
                    }
                    break;
                }
            }
        }
        for(int t : b.keySet()) {
            System.out.println(t+","+b.get(t)+";");
        }
    }
	
}

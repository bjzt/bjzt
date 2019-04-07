package org.bighamapi.hmp.service;

import org.bighamapi.hmp.dao.ArticleDao;
import org.bighamapi.hmp.pojo.Article;
import org.bighamapi.hmp.pojo.Channel;
import org.bighamapi.hmp.pojo.Comment;
import org.bighamapi.hmp.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

/**
 * 服务层
 * 
 * @author bighamapi
 *
 */
@Service
@Transactional
public class ArticleService {

	@Autowired
	private ArticleDao articleDao;
	
	@Autowired
	private IdWorker idWorker;

	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private UserService userService;
	@Autowired
	private ChannelService channelService;

	@Autowired
	private ColumnService columnService;
	@Autowired
	private CommentService commentService;

	public void updateState(String id){
		articleDao.updateState(id);
	}
	/**
	 * 查询全部列表
	 * @return
	 */
	public List<Article> findAll() {
		return articleDao.findAll();
	}

	
	/**
	 * 条件查询+分页
	 * @param whereMap
	 * @param page
	 * @param size
	 * @return
	 */
	public Page<Article> findSearch(Map whereMap, int page, int size) {
		Specification<Article> specification = createSpecification(whereMap);
		PageRequest pageRequest =  PageRequest.of(page-1, size);
		return articleDao.findAll(specification, pageRequest);
	}

	
	/**
	 * 条件查询
	 * @param whereMap
	 * @return
	 */
	public List<Article> findSearch(Map whereMap) {
		Specification<Article> specification = createSpecification(whereMap);
		return articleDao.findAll(specification);
	}

	/**
	 * 根据ID查询实体
	 * @param id
	 * @return
	 */
	public Article findById(String id) {
		//redis
		Article article = null;//(Article) redisTemplate.opsForValue().get("article_"+id);
		if(article == null){
			article = articleDao.findById(id).get();
			//redisTemplate.opsForValue().set("article_"+id, article);
		}
		return article;
	}

	/**
	 * 增加
	 * @param article
	 */
	public void add(Article article) {
		article.setId( idWorker.nextId()+"" );
		article.setCreateTime(new Date());
		article.setUpdateTime(new Date());
		if(userService.findByUsername(article.getUsername()) ==null){
			throw new RuntimeException("用户不存在");
		}
		//如果有专栏，保存
		if (article.getColumn()!=null){
			Map<String,String> map = new HashMap<>();
			map.put("name",article.getColumn().getName());
			if((article.getColumn().getId() == null) && (columnService.findSearch(map) != null)){
				columnService.add(article.getColumn());
			}
		}
		//如果有频道，保存
		if (article.getChannel()!=null){
			List<Channel> channels = article.getChannel();
			for (Channel channel :channels) {
				Map<String,String> map = new HashMap<>();
				map.put("name",channel.getName());
				if (( channel.getId() == null ) && ( channelService.findSearch(map) != null )){
					channelService.add(channel);
				}
			}
		}

		articleDao.save(article);
	}

	/**
	 * 修改
	 * @param article
	 */
	public void update(Article article) {
		redisTemplate.delete("article"+article.getId());
		article.setUpdateTime(new Date());
		articleDao.save(article);
	}

	/**
	 * 删除
	 * @param id
	 */
	public void deleteById(String id) {
		Article byId = findById(id);
		List<Comment> comment = byId.getComment();
		for(Comment comment1 : comment){
			commentService.deleteById(comment1.getId());
		}
//		redisTemplate.delete("article"+id);
		articleDao.deleteById(id);
	}

	/**
	 * 动态条件构建
	 * @param searchMap
	 * @return
	 */
	private Specification<Article> createSpecification(Map searchMap) {

		return new Specification<Article>() {

			@Override
			public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicateList = new ArrayList<Predicate>();
				if(searchMap != null) {
					// ID
					if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
						predicateList.add(cb.like(root.get("id").as(String.class), "%" + (String) searchMap.get("id") + "%"));
					}
					// 专栏
					if (searchMap.get("column") != null && !"".equals(searchMap.get("column"))) {
						predicateList.add(cb.like(root.get("column").as(String.class), "%" + (String) searchMap.get("column") + "%"));
					}
					// 用户名
					if (searchMap.get("username") != null && !"".equals(searchMap.get("username"))) {
						predicateList.add(cb.like(root.get("username").as(String.class), "%" + (String) searchMap.get("username") + "%"));
					}
					// 标题
					if (searchMap.get("title") != null && !"".equals(searchMap.get("title"))) {
						predicateList.add(cb.like(root.get("title").as(String.class), "%" + (String) searchMap.get("title") + "%"));
					}
					// 文章正文
					if (searchMap.get("content") != null && !"".equals(searchMap.get("content"))) {
						predicateList.add(cb.like(root.get("content").as(String.class), "%" + (String) searchMap.get("content") + "%"));
					}
					// 是否公开
					if (searchMap.get("isPublic") != null && !"".equals(searchMap.get("isPublic"))) {
						predicateList.add(cb.like(root.get("isPublic").as(String.class), "%" + (String) searchMap.get("isPublic") + "%"));
					}
					// 是否置顶
					if (searchMap.get("isTop") != null && !"".equals(searchMap.get("isTop"))) {
						predicateList.add(cb.like(root.get("isTop").as(String.class), "%" + (String) searchMap.get("isTop") + "%"));
					}
					// 所属频道
					if (searchMap.get("channel") != null && !"".equals(searchMap.get("channel"))) {
						predicateList.add(cb.like(root.get("channel").as(String.class), "%" + (String) searchMap.get("channel") + "%"));
					}
					// URL
					if (searchMap.get("url") != null && !"".equals(searchMap.get("url"))) {
						predicateList.add(cb.like(root.get("url").as(String.class), "%" + (String) searchMap.get("url") + "%"));
					}
					//时间
					if (!StringUtils.isEmpty(searchMap.get("date_1")) && !StringUtils.isEmpty(searchMap.get("date_2"))) {

					}
				}
				return cb.and( predicateList.toArray(new Predicate[predicateList.size()]));
			}
		};

	}

}

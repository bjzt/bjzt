package org.bighamapi.hmp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.bighamapi.hmp.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.bighamapi.hmp.dao.ChannelDao;
import org.bighamapi.hmp.pojo.Channel;

/**
 * 服务层
 * 
 * @author bighamapi
 *
 */
@Service
@CacheConfig(cacheNames = "channel")
public class ChannelService {

	@Autowired
	private ChannelDao channelDao;
	
	@Autowired
	private IdWorker idWorker;
	/**
	 * 查询全部列表
	 * @return
	 */
	public List<Channel> findAll() {
		return channelDao.findAll();
	}

	
	/**
	 * 条件查询+分页
	 * @param whereMap
	 * @param page
	 * @param size
	 * @return
	 */
	public Page<Channel> findSearch(Map whereMap, int page, int size) {
		Specification<Channel> specification = createSpecification(whereMap);
		PageRequest pageRequest =  PageRequest.of(page-1, size);
		return channelDao.findAll(specification, pageRequest);
	}

	
	/**
	 * 条件查询
	 * @param whereMap
	 * @return
	 */
	public List<Channel> findSearch(Map whereMap) {
		Specification<Channel> specification = createSpecification(whereMap);
		return channelDao.findAll(specification);
	}

	/**
	 * 根据ID查询实体
	 * @param id
	 * @return
	 */
	@Cacheable(key = "#id")
	public Channel findById(String id) {
		return channelDao.findById(id).get();
	}

	/**
	 * 增加
	 * @param channel
	 */
	public void add(Channel channel) {
		channel.setId( idWorker.nextId()+"" );
		channelDao.save(channel);
	}

	/**
	 * 修改
	 * @param channel
	 */
	@CacheEvict(key = "#channel.id")
	public void update(Channel channel) {
		channelDao.save(channel);
	}

	/**
	 * 删除
	 * @param id
	 */
	@CacheEvict(key = "#id")
	public void deleteById(String id) {
		channelDao.deleteById(id);
	}

	/**
	 * 动态条件构建
	 * @param searchMap
	 * @return
	 */
	private Specification<Channel> createSpecification(Map searchMap) {

		return new Specification<Channel>() {

			@Override
			public Predicate toPredicate(Root<Channel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicateList = new ArrayList<Predicate>();
				if (searchMap!= null) {
					if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
						predicateList.add(cb.like(root.get("id").as(String.class), "%" + (String) searchMap.get("id") + "%"));
					}
					//
					if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
						predicateList.add(cb.like(root.get("name").as(String.class), "%" + (String) searchMap.get("name") + "%"));
					}
					//
					if (searchMap.get("state") != null && !"".equals(searchMap.get("state"))) {
						predicateList.add(cb.like(root.get("state").as(String.class), "%" + (String) searchMap.get("state") + "%"));
					}
				}
				return cb.and( predicateList.toArray(new Predicate[predicateList.size()]));

			}
		};

	}

}
